package components

import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._
import org.scalatest.FreeSpec
import chisel3.stage.ChiselStage


class adderTest extends FreeSpec with ChiselScalatestTester {
    "Test adder" in {
    test(new adder){c =>
        c.io.A_in.poke(3.U)
        c.io.B_in.poke(4.U)
        c.io.Cin.poke(0.U)
        c.clock.step(1)
        c.io.Overflow.expect(0.U)
    }
}
}
