package components

import chisel3._
import chisel3.util._
import scala.collection._


class newBundle extends Bundle{
    val a1 = UInt(8.W)
    val a2 = UInt(8.W)
}

class reg extends Module{
    val io = IO(new Bundle{
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_out = Output(UInt(32.W))
    })
    //io.Rd_byte_w_en := 0.U
    //io.Rd_in := 0.U
    val reg = RegInit(VecInit(Seq.fill(4)(0.U(8.W))))
    for(i <- 0 to 3){
        when (io.Rd_byte_w_en(i)){
            reg(i) := io.Rd_in(i * 8 + 7, i * 8)
        }
    }
    io.Rd_out := reg.asUInt()
}

class regFile extends Module {
    val io= IO(new Bundle{
        val Rs_addr = Input(UInt(5.W))
        val Rt_addr = Input(UInt(5.W))
        val Rd_addr = Input(UInt(5.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rs_out = Output(UInt(32.W))
        val Rt_out = Output(UInt(32.W))
    })
    
    //val regs = VecInit(Seq.fill(32)(RegInit(VecInit(Seq.fill(4)(0.U(8.W))))))
    val regs = RegInit(VecInit(Seq.fill(32)(VecInit(Seq.fill(4)(0.U(8.W))))))
    //val regs = VecInit(Seq.fill(32)(Module(new regg)))
    //io.Rs_out := Cat(Seq(regs(io.Rs_addr)(3), regs(io.Rs_addr)(2), regs(io.Rs_addr)(1), regs(io.Rs_addr)(0)))
    //io.Rt_out := Cat(Seq(regs(io.Rt_addr)(3), regs(io.Rt_addr)(2), regs(io.Rt_addr)(1), regs(io.Rt_addr)(0)))
    io.Rs_out := regs(io.Rs_addr).asUInt()
    io.Rt_out := regs(io.Rt_addr).asUInt()
    when(io.Rd_addr =/= 0.U){
        for(i <- 0 to 3){
            when(~io.Rd_byte_w_en(i)){
                regs(io.Rd_addr)(i) := io.Rd_in((i+1)*8 -1 , i*8)
            }
        }
    }
}


class regFile1 extends Module {
    val io= IO(new Bundle{
        val Rs_addr = Input(UInt(5.W))
        val Rt_addr = Input(UInt(5.W))
        val Rd_addr = Input(UInt(5.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rs_out = Output(UInt(32.W))
        val Rt_out = Output(UInt(32.W))
    })
    
    val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
    io.Rs_out := regs(io.Rs_addr)
    io.Rt_out := regs(io.Rt_addr)
    when(io.Rd_addr =/= 0.U){
        val reg = VecInit(regs(io.Rd_addr).asBools())
        for(i <- 0 to 3){ 
            when(io.Rd_byte_w_en(i)){
                for(j <- 0 to 7)
                    reg(i*8 + j) := io.Rd_in(i * 8 + j).asBool()
            }
        }
        regs(io.Rd_addr) := reg.asUInt()
    }
}

class regFile2 extends Module {
    val io= IO(new Bundle{
        val Rs_addr = Input(UInt(5.W))
        val Rt_addr = Input(UInt(5.W))
        val Rd_addr = Input(UInt(5.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rs_out = Output(UInt(32.W))
        val Rt_out = Output(UInt(32.W))
    })
    
    val regs = RegInit(VecInit(Seq.fill(32)(VecInit(Seq.fill(32)(0.U(1.W))))))
    io.Rs_out := regs(io.Rs_addr).asUInt()
    io.Rt_out := regs(io.Rt_addr).asUInt()
    when(io.Rd_addr =/= 0.U){
        for(i <- 0 to 3){ 
            when(io.Rd_byte_w_en(i)){
                for(j <- 0 to 7)
                    regs(io.Rd_addr)(i*8 + j) := io.Rd_in(i * 8 + j)
            }
        }
    }
}

class regFile3 extends Module {
    val io= IO(new Bundle{
        val Rs_addr = Input(UInt(5.W))
        val Rt_addr = Input(UInt(5.W))
        val Rd_addr = Input(UInt(5.W))
        val Rd_in = Input(UInt(32.W))
        val Rd_byte_w_en = Input(UInt(4.W))
        val Rs_out = Output(UInt(32.W))
        val Rt_out = Output(UInt(32.W))
    })
    val regs = VecInit(Seq.fill(32)(Module(new reg).io))
    
    for(i <- 0 to 31){
        regs(i).Rd_byte_w_en := 0.U
        regs(i).Rd_in := 0.U
    }
    io.Rs_out := regs(io.Rs_addr).Rd_out.asUInt()
    io.Rt_out := regs(io.Rt_addr).Rd_out.asUInt()
    when(io.Rd_addr =/= 0.U){
        regs(io.Rs_addr).Rd_byte_w_en := io.Rd_byte_w_en
        regs(io.Rs_addr).Rd_in := io.Rd_in
    }
}
