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


class instrFetchUnit_test(c: instrFetchUnit) extends PeekPokeTester(c){
    poke(c.io.pcInit, "h3fff_ffff".U)
    poke(c.io.reset, 1.U)
    step(1)
    poke(c.io.jump, 1.U)
    poke(c.io.jumpSrc, 0.U)
    poke(c.io.instr, "h3ff_ffff".U)
    step(1)
    expect(c.io.pcOut, "h3fff_ffff".U)
    step(1)
}


object instrFetchUnitGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new instrFetchUnit) {c => new instrFetchUnit_test(c)}
}
