package cpu

import chisel3._
import chisel3.util._
import components._

class program extends Module {
    val io = IO(
      new Bundle {
          val init = Input(
            new Bundle {
                val writeEn = Input(UInt(1.W))
                val addr = Input(UInt(32.W))
                val instr = Input(UInt(32.W))
                val cpuReset = Input(UInt(1.W))
                val pcInit = Input(UInt(32.W))
            }
          )
          val watch = Output(
            new Bundle {
                val regs = Vec(32, UInt(32.W))
                val pc = UInt(32.W)
            }
          )
          val memWatch = Output(Vec(256, UInt(32.W)))
      }
    )
    val cpuUse = Module(new CPU)
    val rom = Mem(256, UInt(32.W))
    cpuUse.io.reset := io.init.cpuReset
    cpuUse.io.pcInit := io.init.pcInit
    cpuUse.io.instr := rom(cpuUse.io.watch.pc)

    when(io.init.writeEn.asBool()) {
        rom(io.init.addr) := io.init.instr
    }
    for (i <- 0 to 255) {
        io.memWatch(i) := rom(i)
    }
    io.watch <> cpuUse.io.watch
}
