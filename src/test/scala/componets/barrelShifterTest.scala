package components

import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._
import org.scalatest.FreeSpec
import chisel3.stage.ChiselStage
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class barrelShifterTest extends FreeSpec with ChiselScalatestTester {
    def getVerilog(dut: => chisel3.core.UserModule): String = {
  import firrtl._
  return chisel3.Driver.execute(Array[String](), {() => dut}) match {
    case s:chisel3.ChiselExecutionSuccess => s.firrtlResultOption match {
      case Some(f:FirrtlExecutionSuccess) => f.emitted
    }
  }
}
    "Test barrelShifter" in {
    test(new barrelShifter){c =>
        c.io.shift_in.poke(128.U)
        c.io.shift_amount.poke(2.U)
        c.io.shift_op.poke(2.U)
        c.io.shift_out.expect(32.U)
        // c.reset.peek()
        // c.io.shift_in.poke(15.U)
        // c.io.shift_op.poke(1.U)
        // c.io.shift_amount.poke(1.U)
        // c.clock.step(1)
        // c.io.shift_out.expect(15.U)
        //println((new ChiselStage).emitVerilog(new barrelShifter))
        // println(getVerilog(new barrelShifter))
        println()
    }
}
}

class barrelShifterTestGen(c: barrelShifter) extends PeekPokeTester(c) {  
    poke(c.io.shift_in, 0xf0000000L.U)
    poke(c.io.shift_amount, 3.U)
    poke(c.io.shift_op, 0.U)
    expect(c.io.shift_out, 0x80000000L.U)
    step(1)//logic left

    poke(c.io.shift_in, 0xf0000000L.U)
    poke(c.io.shift_amount, 3.U)
    poke(c.io.shift_op, 1.U)
    expect(c.io.shift_out, 0x1e000000L.U)
    step(1) // logic right

    poke(c.io.shift_in, 0xf0000000L.U)
    poke(c.io.shift_amount, 2.U)
    poke(c.io.shift_op, 2.U)
    expect(c.io.shift_out, 0xfc000000L.U)
    step(1) // algorithmic right

    poke(c.io.shift_in, 0x30000007L.U)
    poke(c.io.shift_amount, 7.U)
    poke(c.io.shift_op, 3.U)
    expect(c.io.shift_out, 0x0e600000L.U)
    step(1)
}
object barrelShifterGen extends App{
     chisel3.iotesters.Driver.execute(args, () => new barrelShifter)(c => new barrelShifterTestGen(c))
 }

