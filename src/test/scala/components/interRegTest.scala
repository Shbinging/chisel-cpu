package componets

package  components
import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._
import org.scalatest.FreeSpec
import chisel3.stage.ChiselStage
import scala.util.Random
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import _root_.components.AluNew
import _root_.components.AID

class AID_test1(c: AID) extends PeekPokeTester(c){
    poke(c.io.in.ctr.flush, 0.U)
    poke(c.io.in.ctr.exec.RegDst, 1.U)
    poke(c.io.in.data.Rd, 1.U)
    step(1)
    print(peek(c.io.out.ctr.exec))
    print(peek(c.io.in.data))
}

object interRegGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new AID) {c => new AID_test1(c)}
}
