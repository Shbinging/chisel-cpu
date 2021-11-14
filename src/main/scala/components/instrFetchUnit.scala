package components


import chisel3._
import chisel3.util._


class instrFetchUnit extends Module{
    val io = IO(
        new Bundle{
            val pcInit = Input(UInt(30.W))
            val branch = Input(UInt(1.W))
            val branchCond = Input(UInt(2.W))
            val zero = Input(UInt(1.W))
            val less = Input(UInt(1.W))
            val jump = Input(UInt(1.W))
            val jumpSrc = Input(UInt(1.W))
            val instr = Input(UInt(32.W))
            val pcOut = Output(UInt(30.W))
            val busA = Input(UInt(30.W))
            val reset = Input(UInt(1.W))
        }
    )
    //val a = WireInit(io.pcInit)
    val pc = Reg(UInt(30.W))
    when(io.reset.asBool()){
        pc := io.pcInit
        io.pcOut := io.pcInit
    }
    io.pcOut := pc
    val signExtend  = WireInit(Cat(Fill(14, io.instr(15)), io.instr(15, 0)))
    val branchCondRes = WireInit(0.U(1.W))
    switch(io.branchCond){
        is(0.U){
            branchCondRes:= ~io.less;
        }
        is(1.U){
            branchCondRes:= io.less;
        }
        is (2.U){
            branchCondRes := io.zero;
        }
        is (3.U){
            branchCondRes := ~io.zero;
        }
    }
    val pcBranch = Wire(UInt(30.W))
    pcBranch := Mux((io.branch & branchCondRes) === 1.U, (pc.asSInt() + signExtend.asSInt() + 1.S).asUInt(), pc + 1.U)
    
    val pcJump = WireInit(Cat(pc(29,26), io.instr(25, 0)))
    when(io.jumpSrc === 1.U){
        pcJump := io.busA
    }
    when(!io.reset.asBool()){
        when(io.jump === 1.U){
            pc := pcJump
        }.otherwise{
            pc := pcBranch
        }
    }
}