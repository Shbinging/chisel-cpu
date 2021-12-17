package components

import chisel3._
import chisel3.util._

class testif extends Module{
    val io = IO(
        new Bundle{
        val a = Input(UInt(32.W))
        val c = Input(UInt(32.W))
        val b = Output(UInt(32.W))
        }
    )


    when(io.c < 20.U){
        io.b := 3.U
    }
    io.b := 1.U
    when(io.c < 10.U){
        io.b := 2.U
    }
}