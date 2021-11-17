package cpu

import chisel3._
import chisel3.util._
import components._

class program extends Module {
    val io = IO(
      new Bundle {
          val init = new Bundle{
              val writeEn = Input(UInt(1.W))
              val addr = Input(UInt(32.W))
              val instr = Input(UInt(32.W))
          }   
      }
    )
    
}