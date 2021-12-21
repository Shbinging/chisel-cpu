package components

import chisel3._
import chisel3.util._
import scala.collection._

class newBundle extends Bundle {
    val a1 = UInt(8.W)
    val a2 = UInt(8.W)
}

class reg extends Module {
    val io = IO(new Bundle {
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_out = Output(UInt(32.W))
    })
    //io.Rd_byte_w_en := 0.U
    //io.Rd_in := 0.U
    val reg = RegInit(VecInit(Seq.fill(4)(0.U(8.W))))
    for (i <- 0 to 3) {
        when(io.Rd_byte_w_en(i)) {
            reg(i) := io.Rd_in(i * 8 + 7, i * 8)
        }
    }
    io.Rd_out := reg.asUInt()
}

class regBundle extends Bundle {
    val Rs_addr = Input(UInt(5.W))
    val Rt_addr = Input(UInt(5.W))
    val Rd_addr = Input(UInt(5.W))
    val Rd_in = Input(UInt(32.W))
    val Rd_en = Input(UInt(1.W))
    val Rs_out = Output(UInt(32.W))
    val Rt_out = Output(UInt(32.W))
    val watchReg = Output(Vec(32, UInt(32.W)))
}

class regFile extends Module {
    val io = IO(new regBundle)

    //val regs = VecInit(Seq.fill(32)(RegInit(VecInit(Seq.fill(4)(0.U(8.W))))))
    val regs = RegInit(VecInit(Seq.fill(32)(VecInit(Seq.fill(4)(0.U(8.W))))))
    for (i <- 0 to 31) io.watchReg(i) := regs(i).asUInt()
    //val regs = VecInit(Seq.fill(32)(Module(new regg)))
    //io.Rs_out := Cat(Seq(regs(io.Rs_addr)(3), regs(io.Rs_addr)(2), regs(io.Rs_addr)(1), regs(io.Rs_addr)(0)))
    //io.Rt_out := Cat(Seq(regs(io.Rt_addr)(3), regs(io.Rt_addr)(2), regs(io.Rt_addr)(1), regs(io.Rt_addr)(0)))
    io.Rs_out := regs(io.Rs_addr).asUInt()
    io.Rt_out := regs(io.Rt_addr).asUInt()
    // when(io.Rd_en.asBool()){
    //     printf(p"${io.Rd_addr}")
    // }
    when(io.Rd_addr =/= 0.U) {
        for (i <- 0 to 3) {
            when(io.Rd_en.asBool()) {
                regs(io.Rd_addr)(i) := io.Rd_in((i + 1) * 8 - 1, i * 8)
            }
        }
    }
    io.Rs_out := regs(io.Rs_addr).asUInt()
    io.Rt_out := regs(io.Rt_addr).asUInt()
    when(io.Rd_en.asBool() & io.Rs_addr === io.Rd_addr){
        io.Rs_out := io.Rd_in
    }
    when(io.Rd_en.asBool() & io.Rt_addr === io.Rd_addr){
        io.Rt_out := io.Rd_in
    }
    for(i <- 0 to 31){
        io.watchReg(i) := regs(i).asUInt()
    }
}

