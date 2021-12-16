package componets

import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._
import org.scalatest.FreeSpec
import chisel3.stage.ChiselStage
import scala.util.Random
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import _root_.components.mem
import _root_.components.AID

class mem_test(c: mem) extends PeekPokeTester(c){
    poke(c.io.memAddress, 0.U)
    poke(c.io.memWrite, 1.U)
    poke(c.io.memData, 0x12345678L.U)
    step(1)
    poke(c.io.memRead, 1.U)
    poke(c.io.memWrite, 0.U)
    print(peek(c.io.memOut))
    print(peek(c.io.memWatch(0)))
}
object memGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new mem) {c => new mem_test(c)}
}
