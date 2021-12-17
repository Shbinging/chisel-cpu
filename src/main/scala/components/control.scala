package components

import chisel3._
import chisel3.util._

class control extends Module {
    val io = IO(
      new Bundle {
          val instr = Input(UInt(32.W))
          val out = new Bundle {
              val exec = Output(new execCtrBundle)
              val mem = Output(new memCtrBundle)
              val wb = Output(new wbCtrBundle)
          }
      }
    )
    val op = WireInit(io.instr(31, 26))
    val rs = WireInit(io.instr(25, 21))
    val rt = WireInit(io.instr(20, 16))
    val rd = WireInit(io.instr(15, 11))
    val func = WireInit(io.instr(5, 0))
    val shamt = WireInit(io.instr(10, 6))
    val imm = WireInit(io.instr(15, 0))

    io.out.exec.RegDst := 0.U
    io.out.exec.aluOp := 0.U
    io.out.exec.aluSrcA := 0.U
    io.out.exec.aluSrcB := 0.U
    io.out.exec.signExt := 0.U
    io.out.exec.whereToJump := 0.U
    io.out.mem.memRead := 0.U
    io.out.mem.memWrite := 0.U
    io.out.mem.branchCond := 0.U
    io.out.mem.branch := 0.U
    io.out.mem.jump := 0.U
    io.out.wb.memToReg := 0.U
    io.out.wb.wrEn := 0.U

    when(op === 0.U) {
        when(func =/= "b1001".U) {
            when(VecInit("b0".U, "b10".U, "b11".U).contains(func)) {
                io.out.exec.aluSrcA := 3.U
            }
            io.out.exec.RegDst := 1.U
            io.out.wb.memToReg := 1.U
            io.out.wb.wrEn := 1.U
            val aluOp = Wire(Vec(4, UInt(1.W)))
            /*
            a 5
            b 4
            c 3
            d 2
            e 1
            f 0
            abcdef xyzu
            ~a + ~c~d +  ~ce
            a~d + a~e
            f + ~ae + a~c~d
            ~af + ae
             */
            aluOp(3) := (~func(5)) | (~func(3) & ~func(2)) | (~func(3) & func(
              1
            ))
            aluOp(2) := (func(5) & ~func(2)) | (func(5) & ~func(1))
            aluOp(1) := func(0) | (~func(5) & func(1)) | (func(5) & ~func(
              3
            ) & ~func(2))
            aluOp(0) := (~func(5) & func(0)) | (func(5) & func(1))
            io.out.exec.aluOp := aluOp.asUInt()
        }.otherwise {
            when(func === "b1001".U) {
                io.out.exec.aluSrcA := 2.U
                io.out.exec.aluSrcB := 2.U
                io.out.exec.RegDst := 1.U
                io.out.exec.aluOp := "b110".U

                io.out.mem.jump := 1.U
                io.out.wb.memToReg := 1.U
                io.out.wb.wrEn := 1.U
            }
        }
    }.otherwise {
        when(
          VecInit(1.U, "b100".U, "b101".U).contains(op)
        ) {
            io.out.exec.aluSrcB := Mux(op === 1.U, 3.U, 0.U)
            io.out.exec.aluOp := "b101".U
            io.out.mem.branch := 1.U
            switch(op) {
                is(1.U) {
                    when(rt === 1.U) {
                        io.out.mem.branchCond := 0.U
                    }.otherwise {
                        io.out.mem.branchCond := 1.U
                    }
                }
                is("b100".U) {
                    io.out.mem.branchCond := 2.U
                }
                is("b101".U) {
                    io.out.mem.branchCond := 3.U
                }
            }
        }.elsewhen(
          VecInit(
            "b1000".U,
            "b1100".U,
            "b1101".U,
            "b1110".U,
            "b1010".U,
            "b1011".U
          ).contains(op)
        ) {
            io.out.exec.aluSrcB := 1.U
            when(VecInit("b1000".U, "b1010".U, "b1011".U).contains(op)) {
                io.out.exec.signExt := 1.U
            }
            io.out.wb.memToReg := 1.U
            io.out.wb.wrEn := 1.U
            /*
            a 3
            b 2
            c 1
            d 0
            abcd xyzu
            ~b~c + bc
            ~b + ~c
            ~b~c + d
            c
             */
            val aluOp = Wire(Vec(4, UInt(1.W)))
            aluOp(3) := (~op(2) & ~op(1)) | (op(2) & op(1))
            aluOp(2) := (~op(2) | ~op(1))
            aluOp(1) := (~op(2) & ~op(1)) | op(0)
            aluOp(0) := op(1)
            io.out.exec.aluOp := aluOp.asUInt()
        }.otherwise {
            switch(op) {
                is(0.U) {
                    io.out.exec.aluSrcA := 2.U
                    io.out.exec.aluSrcB := 2.U
                    io.out.exec.RegDst := 1.U
                    io.out.exec.aluOp := "b1110".U
                    io.out.mem.jump := 1.U
                    io.out.wb.memToReg := 1.U
                    io.out.wb.wrEn := 1.U
                }
                is("b1111".U) {
                    io.out.exec.aluSrcA := 1.U
                    io.out.exec.aluSrcB := 1.U
                    io.out.exec.RegDst := 0.U
                    io.out.exec.aluOp := "b1000".U
                    io.out.wb.memToReg := 1.U
                    io.out.wb.wrEn := 1.U
                }
                is("b11".U) {
                    io.out.exec.aluSrcA := 2.U
                    io.out.exec.aluSrcB := 2.U
                    io.out.exec.RegDst := 2.U
                    io.out.exec.aluOp := "b1110".U
                    io.out.exec.whereToJump := 1.U
                    io.out.mem.jump := 1.U
                    io.out.wb.memToReg := 1.U
                    io.out.wb.wrEn := 1.U
                }
                is("b101011".U) {
                    io.out.exec.aluSrcB := 1.U
                    io.out.exec.aluOp := "b1110".U
                    io.out.exec.signExt := 1.U
                    io.out.mem.memWrite := 1.U
                }
                is("b100011".U) {
                    io.out.exec.aluSrcB := 1.U
                    io.out.exec.aluOp := "b1110".U
                    io.out.exec.signExt := 1.U
                    io.out.mem.memRead := 1.U
                    io.out.wb.wrEn := 1.U
                }
            }
        }
    }
    // printf("op %d\n", op)
    // printf("rwEn %d\n", io.out.wb.wrEn)
}

class forwarding extends Module {
    val io = IO(
      new Bundle {
          val execwrEn = Input(UInt(1.W))
          val memwrEn = Input(UInt(1.W))
          val execRegDst = Input(UInt(32.W))
          val memRegDst = Input(UInt(32.W))
          val idRaOut = Input(UInt(32.W))
          val idRbOut = Input(UInt(32.W))
          val forwardingA = Output(UInt(2.W))
          val forwardingB = Output(UInt(2.W))
      }
    )
    io.forwardingA := 0.U
    io.forwardingB := 0.U
    when(
      io.memwrEn.asBool() && io.memRegDst === io.idRaOut && io.idRaOut =/= 0.U
    ) {
        io.forwardingA := 2.U
    }
    when(
      io.execwrEn.asBool() && io.execRegDst === io.idRaOut && io.idRaOut =/= 0.U
    ) {
        io.forwardingA := 1.U
    }
    when(
      io.memwrEn.asBool() && io.memRegDst === io.idRbOut && io.idRbOut =/= 0.U
    ) {
        io.forwardingB := 2.U
    }
    when(
      io.execwrEn.asBool() && io.execRegDst === io.idRbOut && io.idRbOut =/= 0.U
    ) {
        io.forwardingB := 1.U
    }
}
