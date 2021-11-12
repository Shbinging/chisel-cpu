package components

import chisel3._
import chisel3.util._


class barrelShifter extends Module{
    val io = IO(new Bundle{
        val shift_in = Input(UInt(32.W))
        val shift_amount = Input(UInt(5.W))
        val shift_op = Input(UInt(2.W))
        val shift_out = Output(UInt(32.W))
    })
    io.shift_out := 0.U
    switch(io.shift_op){
        is(0.U){
            io.shift_out := (io.shift_in << io.shift_amount)(31, 0)
        }
        is (1.U){
            io.shift_out := io.shift_in >> io.shift_amount
        }
        is (2.U){
            io.shift_out := (io.shift_in.asSInt() >> io.shift_amount).asUInt()
        }
        is (3.U){
            io.shift_out := (io.shift_in << (32.U - io.shift_amount)) | (io.shift_in >> io.shift_amount)
        }
    }
}