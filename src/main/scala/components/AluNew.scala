package components

import chisel3._
import chisel3.util._

class AluNew extends Module{
    val io = IO(new Bundle{
            val A_in = Input(UInt(32.W))
            val B_in = Input(UInt(32.W))
            val ALU_op = Input(UInt(4.W))

            val ALU_out = Output(UInt(32.W))
            val Less = Output(UInt(1.W))
            val Overflow_out = Output(UInt(1.W))
            val Zero = Output(UInt(1.W))
    }
    )
    val alu = Module(new Alu)
    val shift = Module(new barrelShifter)
    shift.io.shift_amount := io.A_in(4, 0)
    shift.io.shift_in := io.B_in
    shift.io.shift_op := DontCare
    switch(io.ALU_op){
        is("b1000".U){
            shift.io.shift_op := 0.U
        }
        is("b1010".U){
            shift.io.shift_op := 1.U
        }
        is("b1011".U){
            shift.io.shift_op := 2.U
        }
    }
    alu.io.ALU_op := io.ALU_op
    alu.io.A_in := io.A_in
    alu.io.B_in := io.B_in
    when (VecInit("b1000".U, "b1010".U, "b1011".U).contains(io.ALU_op)){
        io.ALU_out := shift.io.shift_out
        io.Less := 0.U
        io.Overflow_out := 0.U
        io.Zero := Mux(shift.io.shift_out === 0.U, 1.U, 0.U)
    }.otherwise{
        io.ALU_out := alu.io.ALU_out
        io.Less := alu.io.Less
        io.Overflow_out := alu.io.Overflow_out
        io.Zero := alu.io.Zero
    }
}