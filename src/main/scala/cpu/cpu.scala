package cpu
import components._
import chisel3._
import chisel3.util._


class cpu extends Module{
    val io = IO(
        new Bundle{
            val init = new Bundle{
                val reset = Input(UInt(1.W))
                val copyData = Input(Vec(256, UInt(8.W)))
            }
            val watch = new Bundle{
                val regs =  Output(Vec(32, UInt(32.W)))
                val pc = Output(UInt(32.W))
                val data = Output(Vec(256, UInt(8.W)))
            }
        }
    )

    //init
    val instrMem = Module(new mem)
    when(io.init.reset === 1.U){
        pc := 0.U
        instrMem.io.memWrite := 1.U
        for(i <- 0 to 255){
            instrMem.io.memAddress := i.asUInt()
            instrMem.io.memData := io.init.copyData(i)
        }
    }
    when(io.init.reset === 0.U){
        instrMem.io.memWrite := 0.U
        instrMem.io.memRead := 1.U
    }
//IF
//TODO::
    val BAJ = Wire(UInt(32.W))
    val whereToPc = Wire(UInt(1.W))
    
    val pc = RegInit(0.U(32.W))
    pc := Mux(whereToPc === 1.U, BAJ, pc + 4.U)
    instrMem.io.memAddress := pc

    val interAIF = Module(new AIF)
    interAIF.io.in.instr := instrMem.io.memData
    interAIF.io.in.nPc := pc + 4.U
//ID
//TODO::
    val rwEn = Wire(UInt(1.W))
    val rwData = Wire(UInt(32.W))
    val rwAddr = Wire(UInt(32.W))
    val flush = WireInit(0.U(1.W))

    val regs = Module(new regFile)
    regs.io.Rs_addr := interAIF.io.out.instr(25, 21)
    regs.io.Rt_addr := interAIF.io.out.instr(20, 16)
    regs.io.Rd_addr := rwAddr
    regs.io.Rd_en := rwEn
    regs.io.Rd_in := rwData

    val ctr = Module(new control)
    ctr.io.instr := interAIF.io.out.instr

    val interAID = Module(new AID)
    interAID.io.in.ctr.exec <> ctr.io.out.exec
    interAID.io.in.ctr.mem <> ctr.io.out.mem
    interAID.io.in.ctr.wb <> ctr.io.out.wb
    interAID.io.in.ctr.flush := flush
    interAID.io.in.data.Rd := interAIF.io.out.instr(15, 11)
    interAID.io.in.data.Rt := interAIF.io.out.instr(20, 16)
    interAID.io.in.data.imm := interAIF.io.out.instr(15, 0)
    interAID.io.in.data.rbOut := regs.io.Rt_out
    interAID.io.in.data.raOut := regs.io.Rs_out
    interAID.io.in.data.nPc := interAIF.io.out.nPc
    interAID.io.in.data.instrTarget := interAIF.io.out.instr(25, 0)

//EXEC
//TODO::
    val forwardingA = Wire(UInt(2.W))
    val forwardingB = Wire(UInt(2.W))
    val c1 = Wire(UInt(32.W))
    val c2 = Wire(UInt(32.W))


    val raOut = Wire(UInt(32.W))
    val rbOut = Wire(UInt(32.W))
    val imm32 = Wire(UInt(32.W))
    raOut := MuxLookup(forwardingA, 0.U, Array(0.U->interAID.io.out.data.raOut, 1.U -> c1, 2.U -> c2))
    rbOut := MuxLookup(forwardingB, 0.U, Array(0.U->interAID.io.out.data.rbOut, 1.U -> c1, 2.U -> c2))
    imm32 := Mux(interAID.io.out.ctr.exec.signExt === 0.U, Cat(Fill(16, 0.U), interAID.io.out.data.imm), Cat(Fill((16), interAID.io.out.data.imm(15)), interAID.io.out.data.imm))

    val aluSrcA = Wire(UInt(32.W))
    val aluSrcB = Wire(UInt(32.W))
    aluSrcA := MuxLookup(interAID.io.out.ctr.exec.aluSrcA, 0.U, Array(0.U->raOut, 1.U->16.U, 2.U->4.U, 3.U->interAID.io.out.data.imm(5, 0)))
    aluSrcB := MuxLookup(interAID.io.out.ctr.exec.aluSrcB, 0.U, Array(0.U->rbOut, 1.U->imm32, 2.U->interAID.io.out.data.nPc))

    val aluUse = Module(new AluNew)
    aluUse.io.A_in := aluSrcA
    aluUse.io.B_in := aluSrcB
    aluUse.io.ALU_op := interAID.io.out.ctr.exec.aluOp
    
    val interAEXEC = Module(new AEXEC)
    val NPCJ = Wire(UInt(32.W))
    val NPCB = Wire(UInt(32.W))
    NPCJ := Mux(interAID.io.out.ctr.exec.whereToJump === 0.U, raOut, Cat(interAID.io.out.data.nPc(31, 28), interAID.io.out.data.instrTarget, 0.U(2.W)))
    NPCB := (interAID.io.out.data.nPc.asSInt() + imm32.asSInt()).asUInt()

    val regDst = Wire(UInt(5.W))
    regDst := MuxLookup(interAID.io.out.ctr.exec.RegDst, 0.U, Array(0.U->interAID.io.out.data.Rt, 1.U->interAID.io.out.data.Rd, 2.U->31.U))
}