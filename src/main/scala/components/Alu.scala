package  components


import chisel3._
import chisel3.util._

//import scala.collection._
class Alu extends Module{
    val io = IO(
        new Bundle{
            val A_in = Input(UInt(32.W))
            val B_in = Input(UInt(32.W))
            val ALU_op = Input(UInt(4.W))

            val ALU_out = Output(UInt(32.W))
            val Less = Output(UInt(1.W))
            val Overflow_out = Output(UInt(1.W))
            val Zero = Output(UInt(1.W))

            override def toPrintable: Printable = {
                p"================\n"+
                p"ALU_out : ${Hexadecimal(ALU_out)}\n"+
                p"Less : $Less\n"+
                p"Overflow_out : $Overflow_out\n"+
                p"Zero : $Zero\n"+
                p"==================\n"
            }
        }
    )

    val ALU_ctr = Wire(Vec(3, UInt(1.W)))
    val op3 = WireInit(io.ALU_op(3).asUInt())
    val op2 = WireInit(io.ALU_op(2).asUInt())
    val op1 = WireInit(io.ALU_op(1).asUInt())
    val op0 = WireInit(io.ALU_op(0).asUInt())
    ALU_ctr(2) := ~op3 & ~op1 | ~op3 & op2 & op0 | op3 & op1 
    ALU_ctr(1) := ~op3 & ~op2 & ~op1 | op3 & ~op2 & ~op0 | op2 & op1 & ~op0|op3 & op1
    ALU_ctr(0) := ~op2 & ~op1 | ~op3 & op2 & op0 | op3 & op2 & op1
    
    val Adder = Module(new adder)
    Adder.io.A_in := io.A_in
    Adder.io.B_in := io.B_in ^ Fill(32, io.ALU_op(0))
    Adder.io.Cin := io.ALU_op(0)

    when(io.ALU_op === "b0111".U){
        io.Less := ~Adder.io.Carry
    }.otherwise{
        io.Less := Adder.io.Overflow ^ Adder.io.Negative
    }

    when(io.ALU_op(3,1) === "b111".U){
        io.Overflow_out := Adder.io.Overflow
    }.otherwise{
        io.Overflow_out := 0.U
    }

    io.Zero := Adder.io.Zero
    
    io.ALU_out := 0.U
    switch(ALU_ctr.asUInt()){
        is(0.U){
            val vecc = VecInit((io.A_in ^ Fill(32, io.ALU_op(0))).asBools())
            io.ALU_out := 31.U - vecc.lastIndexWhere((c:Bool)=> c)
            //io.ALU_out:= 0.U
        }
        is(1.U){
            io.ALU_out := io.A_in ^ io.B_in
        }
        is(2.U){
            io.ALU_out := io.A_in | io.B_in
        }
        is(3.U){
            io.ALU_out := ~(io.A_in | io.B_in)
        }
        is(4.U){
            io.ALU_out := io.A_in & io.B_in
        }
        is(5.U){
            io.ALU_out := Mux(io.Less.asBool(), 1.U, 0.U)
            //io.ALU_out := 0.U
        }
        is(6.U){
            //io.ALU_out := 0.U
            io.ALU_out := Mux(io.ALU_op(0), Cat(Fill(16, io.B_in(15)), io.B_in(15, 0)), Cat(Fill(24, io.B_in(7)), io.B_in(7, 0)))
        }
        is(7.U){
            io.ALU_out := Adder.io.O_out
        }
    }
    //printf(p"${io.toPrintable}")
}

