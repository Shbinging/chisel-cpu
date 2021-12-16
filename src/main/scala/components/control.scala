package components

import chisel3._
import chisel3.util._

class control extends Module{
    val io = IO(
        new Bundle{
            val instr = Input(UInt(32.W))
            val out = new Bundle{
                val exec = Output(new execCtrBundle)
                val mem = Output(new memCtrBundle)
                val wb = Output(new wbCtrBundle)
            }
        }
    )
    io.out.exec := DontCare
    io.out.mem := DontCare
    io.out.wb := DontCare
}

class forwarding extends Module{
    val io = IO(
        new Bundle{
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
    when(io.memwrEn.asBool() && io.memRegDst === io.idRaOut && io.idRaOut =/= 0.U){
        io.forwardingA := 2.U
    }
    when(io.execwrEn.asBool() && io.execRegDst === io.idRaOut && io.idRaOut  =/= 0.U){
        io.forwardingA := 1.U
    }
    when(io.memwrEn.asBool() && io.memRegDst === io.idRbOut && io.idRbOut  =/= 0.U){
        io.forwardingB := 2.U
    }
    when(io.execwrEn.asBool() && io.execRegDst === io.idRbOut && io.idRbOut  =/= 0.U){
        io.forwardingB := 1.U
    }
}