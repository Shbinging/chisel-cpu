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
    var instrUse = Array(
        538902780L.U,
268435462L.U,
2952658944L.U,
2414149640L.U,
2414215172L.U,
10940450L.U,
2414149632L.U,
10485769L.U,
536936449L.U,
537001986L.U,
2950758400L.U,
2950889468L.U,
603979768L.U,
537067528L.U,
6352905L.U,
603914248L.U,
600047616L.U,
0.U,
0.U,
0.U,
0.U
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
    // while(peek(c.io.watch.pc).asUInt() != 68.U){
    //     step(1)
    // }
    step(60)
    print(peek(c.io.watch.regs(4)).toString()+"\n")
    print(peek(c.io.watch.regs(2)).toString()+"\n")
    //print(peek(c.io.watch.regs(2)).toString()+"\n")
    //print(peek(c.io.watch.regs(2)).toString()+"\n")
}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new cpu) {c => new CPU_test(c)}
}
