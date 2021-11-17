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


class CPU_test(c: CPU) extends PeekPokeTester(c){
    poke(c.io.reset, 1.U)
    poke(c.io.pcInit, 0.U)
    step(1)
    poke(c.io.reset, 0.U)
    poke(c.io.instr, 0xffffffffL.U)
    step(1) 
    step(1) 
    step(1)
    expect(c.io.watch.pc, 0.U)
    step(1)
}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new CPU) {c => new CPU_test(c)}
}
