package cpu

import chisel3._
import chisel3.util._
import components._

class CPU extends Module {
    val io = IO(
      new Bundle {
          val instr = Input(UInt(32.W))
          val pcInit = Input(UInt(32.W))
          val reset = Input(UInt(1.W))
          val watch = Input(new Bundle{
              val a = UInt(32.W)
              val b = UInt(32.W)
          })
      }
    )
    val reg = Module(new regFile)
    val shifter = Module(new barrelShifter)
    val instrU = Module(new instrFetchUnit)
    val aluUse = Module(new Alu)
    val controlUse = Module(new control)
    controlUse.io.instr := io.instr
    val regDst = Wire(UInt(2.W))
    val exceptionEn = Wire(UInt(1.W))
    val regWr = Wire(UInt(1.W))
    val shifterSRC = Wire(UInt(1.W))
    val shifterCtr = Wire(UInt(2.W))
    val extOp = Wire(UInt(1.W))
    val aluSrc = Wire(UInt(2.W))
    val aluCtr = Wire(UInt(4.W))
    val whereToReg = Wire(UInt(3.W))
    //controller
    controlUse.io.instr := io.instr
    regDst := controlUse.io.regDst
    exceptionEn := controlUse.io.exceptionEn
    regWr := controlUse.io.regWr
    shifterSRC := controlUse.io.shifterSRC
    shifterCtr := controlUse.io.shifterCtr
    extOp := controlUse.io.extOp
    aluSrc := controlUse.io.aluSrc
    aluCtr := controlUse.io.aluCtr
    whereToReg := controlUse.io.whereToReg
      //pcUnit
    instrU.io.pcInit := io.pcInit
    instrU.io.reset := io.reset
    instrU.io.branch := controlUse.io.branch
    instrU.io.branchCond := controlUse.io.branchCond
    instrU.io.busA := reg.io.Rs_out
    instrU.io.instr := io.instr
    instrU.io.jump := controlUse.io.jump
    instrU.io.jumpSrc := controlUse.io.jumpSrc
    instrU.io.less := aluUse.io.Less
    instrU.io.zero := aluUse.io.Zero

      //regFile
    reg.io.Rs_addr := io.instr(25, 21)
    reg.io.Rt_addr := io.instr(20, 16)

    reg.io.Rd_addr := 0.U
    switch(regDst) {
        is(0.U) {
            reg.io.Rd_addr := io.instr(20, 16);
        }
        is(1.U) {
            reg.io.Rd_addr := io.instr(15, 11);
        }
        is(2.U) {
            reg.io.Rd_addr := 31.U;
        }
    }

    reg.io.Rd_byte_w_en := Fill(
      4,
      ~(Mux(exceptionEn === 0.U, 1.U, ~aluUse.io.Overflow_out) & regWr)
    )

    val busW = Wire(UInt(32.W))
    reg.io.Rd_in := busW
    //shifter
    shifter.io.shift_amount := Mux(
      shifterSRC === 0.U,
      reg.io.Rs_out(4, 0),
      io.instr(10, 6)
    )
    shifter.io.shift_in := reg.io.Rt_out
    shifter.io.shift_op := shifterCtr

    val extImm = Wire(UInt(32.W))
    extImm := Mux(
      extOp === 0.U,
      Cat(Fill(16, 0.U), io.instr(15, 0)),
      Cat(Fill(16, io.instr(15)), io.instr(15, 0))
    )
    //alu
    aluUse.io.A_in := reg.io.Rs_out
    aluUse.io.B_in := 0.U
    switch(aluSrc) {
        is(0.U) {
            aluUse.io.B_in := reg.io.Rt_out
        }
        is(1.U) {
            aluUse.io.B_in := extImm
        }
        is(2.U) {
            aluUse.io.B_in := 0.U
        }
    }
    aluUse.io.ALU_op := aluCtr

    busW := 0.U
    switch(whereToReg) {
        is(0.U) {
            busW := shifter.io.shift_out
        }
        is(1.U) {
            busW := Cat(io.instr(15, 0), Fill(16, 0.U))
        }
        is(2.U) {
            busW := Cat(instrU.io.pcOut + 2.U, Fill(2, 0.U))
        }
        is(3.U) {
            busW := aluUse.io.ALU_out
        }
        is(4.U) {
            busW := Cat(Fill(31, 0.U), aluUse.io.Less)
        }
    }
    
}
