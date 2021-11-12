package cpu

import chisel3._
import chisel3.util._
import components._


class CPU extends Module{
    val io = IO(
        new Bundle{
            val instr = Input(UInt(32.W))
        }
    )
    val reg = Module(new regFile)
    val shifter = Module(new barrelShifter)
    val 
}