package components

import chisel3._
import chisel3.util._

class expCtrBundle extends Bundle{
    val load = UInt(1.W)
    val canOverFlow = UInt(1.W)
}

class execCtrBundle extends Bundle{
    val aluSrcA = UInt(3.W)
    val aluSrcB = UInt(3.W)
    val RegDst = UInt(2.W)
    val aluOp = UInt(4.W)
    val whereToJump = UInt(2.W)
    val signExt = UInt(1.W)
}

class memCtrBundle extends Bundle{
    val memRead = UInt(1.W)
    val memWrite = UInt(1.W)
    val branchCond = UInt(2.W)
    val branch = UInt(1.W)
    val jump = UInt(1.W)
}

class wbCtrBundle extends Bundle{
    val memToReg = UInt(1.W)
    val wrEn = UInt(1.W)
} 

class ifDataBundle extends Bundle{
    val keep = UInt(1.W)
    val nPc = UInt(32.W)
    val instr = UInt(32.W)
}

class idDataBundle extends Bundle{
    val nPc = UInt(32.W)
    val raOut = UInt(32.W)
    val rbOut = UInt(32.W)
    val imm = UInt(16.W)
    val Rs = UInt(5.W)
    val Rt = UInt(5.W)
    val Rd = UInt(5.W)
    val instrTarget = UInt(26.W)
}

class execDataBundle extends Bundle{
    val nPcJ = UInt(32.W)
    val nPcB = UInt(32.W)
    val zero = UInt(1.W)
    val overflow = UInt(1.W)
    val less = UInt(1.W)
    val aluOut = UInt(32.W)
    val rbOut = UInt(32.W)
    val regDst = UInt(5.W)
}

class memDataBundle extends Bundle{
    val readData = UInt(32.W)
    val aluOut = UInt(32.W)
    val regDst = UInt(5.W)
}

class AIF extends Module{
    val io = IO(new Bundle{
        val in = Input(new ifDataBundle)
        val out = Output(new ifDataBundle)
    })
    val regInstr = Reg(UInt(32.W))
    val npc = Reg(UInt(32.W))
    when (io.in.keep.asBool()){
        regInstr := regInstr
        npc := npc
    }.otherwise{
        regInstr := io.in.instr
        npc := io.in.nPc
    }
    io.out.instr := regInstr
    io.out.nPc := npc
    io.out.keep := DontCare
}

class AID extends Module{
    val io = IO(new Bundle{
        val in = 
            new Bundle{
                val data = Input(new idDataBundle)
                val ctr = new Bundle{
                    val exp = Input(new expCtrBundle)
                    val exec = Input(new execCtrBundle)
                    val mem = Input(new memCtrBundle)
                    val wb = Input(new wbCtrBundle)
                    val flush = Input(UInt(1.W))
                }
            }
        val out = 
            new Bundle{
                val data = Output(new idDataBundle)
                val ctr = new Bundle{
                    val exp = Output(new expCtrBundle)
                    val exec = Output(new execCtrBundle)
                    val mem = Output(new memCtrBundle)
                    val wb = Output(new wbCtrBundle)
                }
            }
        
    })

    val exec = Wire(new execCtrBundle)
    val mem = Wire(new memCtrBundle)
    val wb = Wire(new wbCtrBundle)
    val exp = Wire(new expCtrBundle)
    when(io.in.ctr.flush === 1.U){
        val tmpWire = WireInit(0.U(300.W))
        exp:= tmpWire.asTypeOf(new expCtrBundle)
        exec := tmpWire.asTypeOf(new execCtrBundle)
        mem := tmpWire.asTypeOf(new memCtrBundle)
        wb := tmpWire.asTypeOf(new wbCtrBundle)
    }.otherwise{
        exp <> io.in.ctr.exp
        exec <> io.in.ctr.exec
        mem <> io.in.ctr.mem
        wb <> io.in.ctr.wb
    }
    io.out.data <> RegNext(io.in.data)
    io.out.ctr.exp <> RegNext(exp)
    io.out.ctr.exec <> RegNext(exec)
    io.out.ctr.mem <> RegNext(mem)
    io.out.ctr.wb <> RegNext(wb)
    // printf("flush-rwEn %d %d %d\n", io.in.ctr.flush, wb.wrEn, io.out.ctr.wb.wrEn)
}

class AEXEC extends Module{
    val io = IO(new Bundle{
        val in = new Bundle{
            val ctr = new Bundle{
                val mem = Input(new memCtrBundle)
                val wb = Input(new wbCtrBundle)
                val flush = Input(UInt(1.W))
            }
            val data = Input(new execDataBundle)
        }
        val out = new Bundle{
            val ctr = new Bundle{
                val mem = Output(new memCtrBundle)
                val wb = Output(new wbCtrBundle)
            }
            val data = Output(new execDataBundle)
        }
    }
    )

    val mem = Wire(new memCtrBundle)
    val wb = Wire(new wbCtrBundle)

    when(io.in.ctr.flush === 1.U){
        val tmpWire = WireInit(0.U(300.W))
        mem := tmpWire.asTypeOf(new memCtrBundle)
        wb := tmpWire.asTypeOf(new wbCtrBundle)
    }.otherwise{
        mem <> io.in.ctr.mem
        wb <> io.in.ctr.wb
    }
    io.out.data <> RegNext(io.in.data)
    io.out.ctr.mem <> RegNext(mem)
    io.out.ctr.wb <> RegNext(wb)
}

class AMEM extends Module{
    val io = IO(new Bundle{
        val in = new Bundle{
            val ctr = new Bundle{
                val wb = Input(new wbCtrBundle)
                val flush = Input(UInt(1.W))
            }
            val data = Input(new memDataBundle)
        }
        val out = new Bundle{
            val ctr = new Bundle{
                val wb = Output(new wbCtrBundle)
            }
            val data = Output(new memDataBundle)
        }
    }
    )

    val wb = Wire(new wbCtrBundle)

    when(io.in.ctr.flush === 1.U){
        val tmpWire = WireInit(0.U(300.W))
        wb := tmpWire.asTypeOf(new wbCtrBundle)
    }.otherwise{
        wb <> io.in.ctr.wb
    }
    io.out.data <> RegNext(io.in.data)
    io.out.ctr.wb <> RegNext(wb)
}