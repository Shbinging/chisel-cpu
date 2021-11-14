package components

import chisel3._
import chisel3.util._

class control extends Module {
    val io = IO(
      new Bundle {
          val instr = Input(UInt(32.W))
          val regDst = Output(UInt(2.W))
          val exceptionEn = Output(UInt(1.W))
          val regWr = Output(UInt(1.W))
          val shifterSRC = Output(UInt(1.W))
          val shifterCtr = Output(UInt(2.W))
          val extOp = Output(UInt(1.W))
          val aluSrc = Output(UInt(2.W))
          val aluCtr = Output(UInt(4.W))
          val whereToReg = Output(UInt(3.W))
          val branch = Output(UInt(1.W))
          val branchCond = Output(UInt(2.W))
          val jump = Output(UInt(1.W))
          val jumpSrc = Output(UInt(1.W))
      }
    )
    io.regDst := 0.U
    io.exceptionEn := 0.U
    io.regWr := 0.U
    io.shifterSRC := 0.U
    io.shifterCtr := 0.U
    io.extOp := 0.U
    io.aluSrc := 0.U
    io.aluCtr := 0.U
    io.whereToReg := 0.U
    io.branch := 0.U
    io.branchCond := 0.U
    io.jump := 0.U
    io.jumpSrc := 0.U
    val op = WireInit(io.instr(31, 26))
    val func = WireInit(io.instr(5, 0))
    when(op === 0.U) { //R type
        io.regWr := 1.U
        io.regDst := 1.U
        val exceptionEnList = VecInit("b100000".U, "b100010".U)
        io.exceptionEn := exceptionEnList.contains(func).asUInt()
        val shifterSRCList = VecInit("b000000".U, "b000010".U, "b000011".U)
        io.shifterSRC := shifterSRCList.contains(func).asUInt()
        io.shifterCtr := Cat(func(0), func(1) & (~func(0)))
        //whereToReg
        when(func === "b001001".U){
            io.whereToReg := 2.U
        }.elsewhen(func(5) === 0.U){
            io.whereToReg := 0.U
        }.elsewhen(func(3) === 1.U){
            io.whereToReg := 4.U
        }.otherwise{
            io.whereToReg := 3.U
        }
        /*
        a 5
        b 4
        c 3
        d 2
        e 1
        f 0
        (~d + e) ~c
        ~d + ~e
        ~c ~d + f
        e
        */
        io.aluCtr := Cat(((~func(2) | func(1)) & ~func(3)), ~func(2) | ~func(1), (~func(3) & ~func(2)) | func(0), func(1))
        io.aluSrc := 0.U
        io.branch := 0.U
        io.branchCond := 0.U
        io.jump := 0.U
        io.jumpSrc := 0.U
        when (func === "b001001".U){
            io.jump := 1.U
            io.jumpSrc := 1.U
        }
    }.elsewhen(VecInit("b000_001".U, "b000_100".U, "b000101".U).contains(op)){//bgez bltz bne beq 
        io.regWr := 0.U;
        io.aluCtr := "b0101".U
        io.aluSrc := Mux(op === "b000_001".U, 2.U, 0.U)
        io.branch := 1.U
        when(op === "b000_001".U){
            when(io.instr(20, 16) === 0.U){
                io.branchCond := 0.U
            }.otherwise{
                io.branchCond := 1.U
            }
        }.elsewhen(op === "b100".U){
            io.branchCond := 2.U
        }.elsewhen(op === "b101".U){
            io.branchCond := 3.U
        }
        io.jump := 0.U
    }.otherwise{//other
        io.regWr := 1.U;
        io.regDst := Mux(op === 3.U, 2.U, 0.U)
        io.exceptionEn := Mux(op === "b1000".U, 1.U, 0.U)
        when(VecInit("b1010".U, "b1011".U).contains(op)){
            io.whereToReg := 4.U;
        }.elsewhen(op === "b1111".U){
            io.whereToReg := 1.U
        }.elsewhen(op === "b11".U){
            io.whereToReg := 2.U
        }.otherwise{
            io.whereToReg := 3.U
        }
        /*
        a 5
        b 4
        c 3
        d 2
        e 1
        f 0
        (~d~e) +de
        ~d + ~e
        ~e ~d + f
        e
        */
        io.aluCtr := Cat((~op(2) & ~ op(1)) | (op(2) & op(1)), ~op(2) | ~op(1), (~op(1) & ~op(2)) | op(0), op(1))
        io.aluSrc := 1.U
        when(VecInit("b1000".U, "b1010".U, "b1011".U).contains(op)){
            io.extOp := 1.U
        }.otherwise{
            io.extOp := 0.U
        }
        io.branch := 0.U
        io.jump := 0.U
        when(op === "b000011".U){
            io.jump := 1.U
        }
    }
}
