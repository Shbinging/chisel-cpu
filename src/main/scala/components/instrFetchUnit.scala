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
        }
    )
    val pc = RegInit(io.pcInit)
    io.pcOut := pc
    val signExtend  = WireInit(cat(Fill(14, io.instr(15)), io.instr(15, 0)))
    val pcNew = WireInit(pc + 1.U)
    val pcBranch = WireInit(pcNew + signExtend.asSInt())
    val branchCondRes = Wire(UInt(1.W))
    switch(io.branch){
        is(0.U){
            branchCondRes:= ~less;
        }
        is(1.U){
            branchCondRes:= less;
        }
        is (2.U){
            branchCondRes := zero;
        }
        is (3.U){
            branchCondRes := ~zero;
        }
    }
    val jumpPc = WireInit(cat(pc(30,27), io.instr(25, 0)))
    when(io.jumpSrc == 1.U){
        jumpPc := busA
    }
    when(branchCondRes && branch){
        pcNew := pcBranch
    }
    when(io.jump){
        pc := jumpPc
    }.otherwise{
        pc := pcNew
    }
}