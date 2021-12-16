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
    for(i <- 0 to 255){
        poke(c.io.init.copyData(i), 0.U)
    }
}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new cpu) {c => new CPU_test(c)}
}
