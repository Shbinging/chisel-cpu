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


class CPU_test(c: CPU) extends PeekPokeTester(c){
//set pc = 4026531840 
poke(c.io.instr, 1006759935L.U);step(1);poke(c.io.instr, 874643440L.U);step(1);poke(c.io.instr, 2101257L.U);step(1);poke(c.io.instr, 1006698496L.U);step(1);poke(c.io.instr, 874577920L.U);step(1);poke(c.io.instr, 1006764032L.U);step(1);poke(c.io.instr, 876740608L.U);step(1);
//jal 0
poke(c.io.instr, 201326592L.U);step(1)
//watch.pc expect 4026531840U -268435456S
expect(c.io.watch.pc, 4026531840L.U, "1")
//set pc = 4026531840 
poke(c.io.instr, 1006759935L.U);step(1);poke(c.io.instr, 874643440L.U);step(1);poke(c.io.instr, 2101257L.U);step(1);poke(c.io.instr, 1006698496L.U);step(1);poke(c.io.instr, 874577920L.U);step(1);poke(c.io.instr, 1006764032L.U);step(1);poke(c.io.instr, 876740608L.U);step(1);
//jal 67108863
poke(c.io.instr, 268435455L.U);step(1)
//watch.pc expect 4294967292U -4S
expect(c.io.watch.pc, 4294967292L.U, "2")
//set pc = 4026531840 
poke(c.io.instr, 1006759935L.U);step(1);poke(c.io.instr, 874643440L.U);step(1);poke(c.io.instr, 2101257L.U);step(1);poke(c.io.instr, 1006698496L.U);step(1);poke(c.io.instr, 874577920L.U);step(1);poke(c.io.instr, 1006764032L.U);step(1);poke(c.io.instr, 876740608L.U);step(1);
//jal 0
poke(c.io.instr, 201326592L.U);step(1)
//watch.pc expect 4026531840U -268435456S
expect(c.io.watch.pc, 4026531840L.U, "3")
//set pc = 4026531840 
poke(c.io.instr, 1006759935L.U);step(1);poke(c.io.instr, 874643440L.U);step(1);poke(c.io.instr, 2101257L.U);step(1);poke(c.io.instr, 1006698496L.U);step(1);poke(c.io.instr, 874577920L.U);step(1);poke(c.io.instr, 1006764032L.U);step(1);poke(c.io.instr, 876740608L.U);step(1);
//jal 1
poke(c.io.instr, 201326593L.U);step(1)
//watch.pc expect 4026531844U -268435452S
expect(c.io.watch.pc, 4026531844L.U, "4")
//set pc = 4026531840 
poke(c.io.instr, 1006759935L.U);step(1);poke(c.io.instr, 874643440L.U);step(1);poke(c.io.instr, 2101257L.U);step(1);poke(c.io.instr, 1006698496L.U);step(1);poke(c.io.instr, 874577920L.U);step(1);poke(c.io.instr, 1006764032L.U);step(1);poke(c.io.instr, 876740608L.U);step(1);
//jal 67108863
poke(c.io.instr, 268435455L.U);step(1)
//watch.pc expect 4294967292U -4S
expect(c.io.watch.pc, 4294967292L.U, "5")



}

object cpuGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new CPU) {c => new CPU_test(c)}
}
