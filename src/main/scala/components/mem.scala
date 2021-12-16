package components

import chisel3._
import chisel3.util._

class mem extends Module{
    val io = IO(
        new Bundle{
            val memRead = Input(UInt(1.W))
            val memWrite = Input(UInt(1.W))
            val memAddress = Input(UInt(32.W))
            val memData = Input(UInt(32.W))
            val memOut = Output(UInt(32.W))
            val memWatch = Output(Vec(64, UInt(32.W)))
        }
    )

    val memUse = Mem(256, UInt(8.W))
    io.memOut := 0.U
    when(io.memRead === 1.U){
        io.memOut := VecInit(memUse(io.memAddress + 3.U), memUse(io.memAddress + 2.U), memUse(io.memAddress + 1.U), memUse(io.memAddress)).asUInt()
    }
    when(io.memWrite === 1.U){
        val vec = VecInit(io.memData.asBools)
        memUse(io.memAddress) := io.memData(31, 24).asUInt()
        memUse(io.memAddress + 1.U) := io.memData(23, 16).asUInt()
        memUse(io.memAddress + 2.U) := io.memData(15, 8).asUInt()
        memUse(io.memAddress + 3.U) := io.memData(7, 0).asUInt() 
    }
    for(i <- 0 to 63){
        io.memWatch(i) := VecInit(memUse(i * 4 + 3), memUse(i * 4 + 2), memUse(i * 4 + 1), memUse(i * 4)).asUInt()
    }
}