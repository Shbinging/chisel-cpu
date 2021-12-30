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
    var instrUse = Array(536936448L.U,
537001985L.U,
537133066L.U,
679804931L.U,
335872007L.U,
545718269L.U,
79691786L.U,
2242592L.U,
541130752L.U,
551682048L.U,
549912575L.U,
268500986L.U,
545652734L.U,
77594626L.U,
537001985L.U,
268435457L.U,
537001984L.U,
)
    for(i <- 0 to 63){
        poke(c.io.init.writeAddr, (i * 4).asUInt());
        if (i < instrUse.length){
            poke(c.io.init.copyData, instrUse(i));
        }else{
            poke(c.io.init.copyData, 0xffffffffL.U)
        }
        step(1)
    }
    step(1)
    poke(c.io.init.reset, 0.U)
    step(1000)
    print(peek(c.io.watch.regs(4)).toString()+"\n")
    print(peek(c.io.watch.regs(2)).toString()+"\n")
    //print(peek(c.io.watch.regs(2)).toString()+"\n")
    //print(peek(c.io.watch.regs(2)).toString()+"\n")
}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new cpu) {c => new CPU_test(c)}
}
