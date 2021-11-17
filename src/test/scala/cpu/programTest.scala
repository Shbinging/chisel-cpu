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


class program_test(c: program) extends PeekPokeTester(c){
    poke(c.io.init.writeEn, 1.U)
    poke(c.io.init.addr, 0.U)
    poke(c.io.init.instr, 539033616L.U)
    poke(c.io.init.pcInit, 0.U)
    poke(c.io.init.cpuReset, 1.U)
    step(1)
    poke(c.io.init.cpuReset, 0.U)
    step(1)
    expect(c.io.memWatch(0), 539033616L.U)
    expect(c.io.watch.regs(1), 16.U)
}

object programGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new program) {c => new program_test(c)}
}