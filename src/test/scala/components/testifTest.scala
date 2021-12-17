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

class testif_test(c: testif) extends PeekPokeTester(c){
    poke(c.io.c, 0.U)
    print(peek(c.io.b))
}
object testifGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new testif) {c => new testif_test(c)}
}



