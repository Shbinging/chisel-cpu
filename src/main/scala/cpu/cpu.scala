package cpu
import components._
import chisel3._
import chisel3.util._

class cpu extends Module {
    val io = IO(
      new Bundle {
          val init = new Bundle {
              val reset = Input(UInt(1.W))
              val copyData = Input(UInt(32.W))
              val writeAddr = Input(UInt(32.W))
          }
          val watch = new Bundle {
              val regs = Output(Vec(32, UInt(32.W)))
              val pc = Output(UInt(32.W))
              val data = Output(Vec(64, UInt(32.W)))
              val instr = Output(Vec(64, UInt(32.W)))
          }
      }
    )
    val roundReg = Reg(UInt(32.W))
    when(io.init.reset === 1.U){
        roundReg := 0.U
    }.otherwise{
        roundReg := roundReg + 1.U
    }
//IF
//TODO::
    //val pcEn = Wire(UInt(1.W))
    //val pc = RegEnable(UInt(32.W), 0.W, pcEn.asBool())
    val pc = Reg(UInt(32.W))
    val flush = WireInit(0.U(1.W))
    val whereToPc = Wire(UInt(1.W))
    val BAJ = Wire(UInt(32.W))
    val instrMem = Module(new mem)
    instrMem.io.memRead := 1.U
    instrMem.io.memWrite := 0.U
    instrMem.io.memData := 0.U
    pc := Mux(whereToPc === 1.U, BAJ, pc + 4.U)
    instrMem.io.memAddress := pc
    whereToPc := 0.U
    flush := 0.U

    val interAIF = Module(new AIF)
    interAIF.io.in.instr := Mux(flush === 0.U, instrMem.io.memOut, 0.U)
    interAIF.io.in.nPc := pc + 4.U
    when(io.init.reset === 0.U) {
        printf("clock%d\n", roundReg + 1.U)
        printf("pc%d\n", pc)
        printf("======IF========\n")
        printf(p"${interAIF.io.in}\n")
    }
//ID
//TODO::
    val rwEn = Wire(UInt(1.W))
    val rwData = Wire(UInt(32.W))
    val rwAddr = Wire(UInt(5.W))

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
    interAID.io.in.ctr.exp <> ctr.io.out.exp
    interAID.io.in.ctr.flush := flush
    interAID.io.in.data.Rs := interAIF.io.out.instr(25, 21)
    interAID.io.in.data.Rd := interAIF.io.out.instr(15, 11)
    interAID.io.in.data.Rt := interAIF.io.out.instr(20, 16)
    interAID.io.in.data.imm := interAIF.io.out.instr(15, 0)
    interAID.io.in.data.rbOut := regs.io.Rt_out
    interAID.io.in.data.raOut := regs.io.Rs_out
    interAID.io.in.data.nPc := interAIF.io.out.nPc
    interAID.io.in.data.instrTarget := interAIF.io.out.instr(25, 0)

    when(io.init.reset === 0.U) {
        printf("")
        printf("======ID========\n")
        printf(p"${interAID.io.in.data}\n")
        printf(p"${interAID.io.in.ctr.exec}\n")
        printf(p"${interAID.io.in.ctr.mem}\n")
        printf(p"${interAID.io.in.ctr.wb}\n")
        printf(p"${interAID.io.in.ctr.exp}\n")
    }
//EXEC
//TODO::
    val forwardingA = Wire(UInt(2.W))
    val forwardingB = Wire(UInt(2.W))
    val c1 = Wire(UInt(32.W))
    val c2 = Wire(UInt(32.W))

    val raOut = Wire(UInt(32.W))
    val rbOut = Wire(UInt(32.W))
    val imm32 = Wire(UInt(32.W))
    raOut := MuxLookup(
      forwardingA,
      0.U,
      Array(0.U -> interAID.io.out.data.raOut, 1.U -> c1, 2.U -> c2)
    )
    rbOut := MuxLookup(
      forwardingB,
      0.U,
      Array(0.U -> interAID.io.out.data.rbOut, 1.U -> c1, 2.U -> c2)
    )
    imm32 := Mux(
      interAID.io.out.ctr.exec.signExt === 0.U,
      Cat(Fill(16, 0.U), interAID.io.out.data.imm),
      Cat(Fill((16), interAID.io.out.data.imm(15)), interAID.io.out.data.imm)
    )

    val aluSrcA = Wire(UInt(32.W))
    val aluSrcB = Wire(UInt(32.W))
    aluSrcA := MuxLookup(
      interAID.io.out.ctr.exec.aluSrcA,
      0.U,
      Array(
        0.U -> raOut,
        1.U -> 16.U,
        2.U -> 0.U,
        3.U -> interAID.io.out.data.imm(10, 6)
      )
    )
    aluSrcB := MuxLookup(
      interAID.io.out.ctr.exec.aluSrcB,
      0.U,
      Array(
        0.U -> rbOut,
        1.U -> imm32,
        2.U -> interAID.io.out.data.nPc,
        3.U -> 0.U
      )
    )

    val aluUse = Module(new AluNew)
    aluUse.io.A_in := aluSrcA
    aluUse.io.B_in := aluSrcB
    aluUse.io.ALU_op := interAID.io.out.ctr.exec.aluOp

    val interAEXEC = Module(new AEXEC)
    val NPCJ = Wire(UInt(32.W))
    val NPCB = Wire(UInt(32.W))
    NPCJ := Mux(
      interAID.io.out.ctr.exec.whereToJump === 0.U,
      raOut,
      Cat(
        interAID.io.out.data.nPc(31, 28),
        interAID.io.out.data.instrTarget,
        0.U(2.W)
      )
    )
    NPCB := (interAID.io.out.data.nPc.asSInt() + imm32.asSInt() << 2).asUInt()

    val regDst = Wire(UInt(5.W))
    regDst := MuxLookup(
      interAID.io.out.ctr.exec.RegDst,
      0.U,
      Array(
        0.U -> interAID.io.out.data.Rt,
        1.U -> interAID.io.out.data.Rd,
        2.U -> 31.U
      )
    )

    val loadStallUse = Module(new loadStall)
    loadStallUse.io.regDst := regDst
    loadStallUse.io.isLoad := interAID.io.out.ctr.exp.load
    loadStallUse.io.instr := interAIF.io.out.instr
    when(loadStallUse.io.aidFlush === 1.U){
        interAID.io.in.ctr.flush := 1.U
    }
    interAIF.io.in.keep := loadStallUse.io.keep
    when(loadStallUse.io.pcEn === 0.U){
        pc := pc
    }
    interAEXEC.io.in.data.aluOut := aluUse.io.ALU_out
    interAEXEC.io.in.data.less := aluUse.io.Less
    interAEXEC.io.in.data.overflow := aluUse.io.Overflow_out
    interAEXEC.io.in.data.zero := aluUse.io.Zero
    interAEXEC.io.in.data.rbOut := rbOut
    interAEXEC.io.in.data.regDst := regDst
    interAEXEC.io.in.data.nPcB := NPCB
    interAEXEC.io.in.data.nPcJ := NPCJ
    interAEXEC.io.in.ctr.flush := flush
    interAEXEC.io.in.ctr.mem <> interAID.io.out.ctr.mem
    interAEXEC.io.in.ctr.wb <> interAID.io.out.ctr.wb
    when(interAID.io.out.ctr.exp.canOverFlow === 1.U && aluUse.io.Overflow_out === 1.U){
        interAEXEC.io.in.ctr.wb.wrEn := 0.U
    }
    when(io.init.reset === 0.U) {
        printf("======EXEC========\n")
        printf(p"${aluUse.io}\n")
        printf(p"${interAEXEC.io.in.data}\n")
        printf(p"${interAEXEC.io.in.ctr.mem}\n")
        printf(p"${interAEXEC.io.in.ctr.wb}\n")
    }
//MEM

    val needBranch = WireInit(0.U(1.W))
    when(interAEXEC.io.out.ctr.mem.branch === 1.U) {
        switch(interAEXEC.io.out.ctr.mem.branchCond) {
            is(0.U) {
                needBranch := ~interAEXEC.io.out.data.less
            }
            is(1.U) {
                needBranch := interAEXEC.io.out.data.less
            }
            is(2.U) {
                needBranch := interAEXEC.io.out.data.zero
            }
            is(3.U) {
                needBranch := ~interAEXEC.io.out.data.zero
            }
        }
    }

    flush := whereToPc
    BAJ := Mux(
      needBranch === 1.U,
      interAEXEC.io.out.data.nPcB,
      interAEXEC.io.out.data.nPcJ
    )
    whereToPc := needBranch | interAEXEC.io.out.ctr.mem.jump

    val dataMem = Module(new mem)
    dataMem.io.memAddress := interAEXEC.io.out.data.aluOut
    dataMem.io.memData := interAEXEC.io.out.data.rbOut
    dataMem.io.memRead := interAEXEC.io.out.ctr.mem.memRead
    dataMem.io.memWrite := interAEXEC.io.out.ctr.mem.memWrite

    val interAMEM = Module(new AMEM)
    interAMEM.io.in.ctr.wb <> interAEXEC.io.out.ctr.wb
    interAMEM.io.in.ctr.flush := 0.U
    interAMEM.io.in.data.aluOut := interAEXEC.io.out.data.aluOut
    interAMEM.io.in.data.regDst := interAEXEC.io.out.data.regDst
    interAMEM.io.in.data.readData := dataMem.io.memOut

    when(io.init.reset === 0.U) {
        printf("======MEM========\n")
        printf(p"${interAMEM.io.in.data}\n")
        printf(p"${interAMEM.io.in.ctr.wb}\n")
    }

//WB
    rwAddr := interAMEM.io.out.data.regDst
    rwData := Mux(
      interAMEM.io.out.ctr.wb.memToReg === 0.U,
      interAMEM.io.out.data.readData,
      interAMEM.io.out.data.aluOut
    )
    rwEn := interAMEM.io.out.ctr.wb.wrEn

    when(io.init.reset === 0.U) {
        printf("======WB========\n")
        printf(p"rwAddr ${rwAddr}\n")
        printf(p"rwData ${rwData}\n")
        printf(p"rwEn ${rwEn}\n")
    }

//forwarding

    val fwd = Module(new forwarding)
    fwd.io.execRegDst := interAEXEC.io.out.data.regDst
    fwd.io.execwrEn := interAEXEC.io.out.ctr.wb.wrEn
    fwd.io.memRegDst := rwAddr
    fwd.io.memwrEn := rwEn
    fwd.io.idRaOut := interAID.io.out.data.Rs
    fwd.io.idRbOut := interAID.io.out.data.Rt
    forwardingA := fwd.io.forwardingA
    forwardingB := fwd.io.forwardingB
    c1 := interAEXEC.io.out.data.aluOut
    c2 := rwData
    when(io.init.reset === 0.U) {
        printf("======forward======\n")
        printf(p"flush ${flush}\n")
        printf(p"${fwd.io}\n")
        printf("==============\n")
        printf("======loadStall======\n")
        printf(p"loadStall ${loadStallUse.io}\n")
        printf("==============\n\n")
    }
    //watch
    io.watch.pc := pc
    for (i <- 0 to 31) {
        io.watch.regs(i) := regs.io.watchReg(i)
    }
    for (i <- 0 to 63) {
        io.watch.data(i) := dataMem.io.memWatch(i)
        io.watch.instr(i) := instrMem.io.memWatch(i)
    }

    when(io.init.reset === 1.U) {
        pc := 0.U
        instrMem.io.memWrite := 1.U
        instrMem.io.memRead := 0.U
        instrMem.io.memAddress := io.init.writeAddr
        instrMem.io.memData := io.init.copyData
        interAIF.io.in.instr := 0xffffffffL.U
        flush := 1.U
    }

    when(io.init.reset === 0.U && instrMem.io.memOut === 0xffffffffL.U){
        pc := pc
    }//halt
}
