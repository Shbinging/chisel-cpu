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


class control_test(c: control) extends PeekPokeTester(c){
 

}

class forwarding_test(c: forwarding) extends PeekPokeTester(c){
    poke(c.io.idRbOut, 1.U)
    poke(c.io.idRaOut, 2.U)
    poke(c.io.execRegDst, 2.U)
    poke(c.io.memRegDst, 1.U)
    poke(c.io.memwrEn, 0.U)
    poke(c.io.execwrEn, 1.U)
    print(peek(c.io.forwardingA).toString() + "\n")
    print(peek(c.io.forwardingB).toString() + "\n")
}
object controlGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new control) {c => new control_test(c)}
}

object forwardingGen extends App{
    chisel3.iotesters.Driver.execute(args, () => new forwarding){c => new forwarding_test(c)}
}


