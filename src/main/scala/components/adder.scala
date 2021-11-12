package components

import chisel3._
import chisel3.util._
import scala.collection._

class adder extends Module{
    val io = IO(
        new Bundle{
            val A_in = Input(UInt(32.W))
            val B_in = Input(UInt(32.W))
            val Cin = Input(UInt(1.W))
            val Zero = Output(UInt(1.W))
            val Carry = Output(UInt(1.W))
            val Overflow = Output(UInt(1.W))
            val Negative = Output(UInt(1.W))
            val O_out = Output(UInt(32.W))
            override def toPrintable: Printable = {
                p"================\n"+
                p"${Cin.toString()} : $Cin\n"+
                p"Zero : $Zero\n"+
                p"Carry : $Carry\n"+
                p"Overflow : $Overflow\n"+
                p"Negative: $Negative\n"+
                p"O_out: ${Hexadecimal(O_out)}\n"+
                p"==================\n"
            }
        }
    )
    // printf("okk::%d\n", io.A_in)

    val res = WireInit(io.A_in +& io.B_in +& io.Cin)
    io.Carry := res(32)
    io.Zero := (res(31, 0) === 0.U)
    io.Overflow := ((~io.A_in(31)) & (~io.B_in(31)) & (res(31))) | ( io.A_in(31) & io.B_in(31) & (!res(31)))
    io.Negative := res(31)
    io.O_out := res(31, 0)
    //printf(p"$io")
    //rintf(p"$io.")
    // printf("%d\n",res)
    // printf("io.Carry:%d\n", io.Carry)
    // printf("io.Zero:%d\n", io.Zero)
    // printf("io.Overflow:%d\n", io.Overflow)
    // printf("io.Negative:%d\n", io.Negative)
    // printf("io.O_out:%d\n", io.O_out)
}