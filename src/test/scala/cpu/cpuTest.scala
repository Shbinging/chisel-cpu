package  cpu
import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._
import org.scalatest.FreeSpec
import chisel3.stage.ChiselStage
import scala.util.Random
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class CPU_test(c: cpu) extends PeekPokeTester(c){
    poke(c.io.init.reset, 1.U)
    var instrUse = Array(268435459L.U,536936458L.U,539099141L.U,541130762L.U,0.U,0.U,0.U,0.U)
    for(i <- 0 to 63){
        poke(c.io.init.writeAddr, (i * 4).asUInt());
        if (i < instrUse.length){
            poke(c.io.init.copyData, instrUse(i));
        }else{
            poke(c.io.init.copyData, 0xffffffffL.U)
        }
        step(1)
    }
    step(1)
    poke(c.io.init.reset, 0.U)
    step(9)
    print(peek(c.io.watch.regs(1)).toString()+"\n")
    print(peek(c.io.watch.regs(2)).toString()+"\n")
    //print(peek(c.io.watch.regs(2)).toString()+"\n")
}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new cpu) {c => new CPU_test(c)}
}
