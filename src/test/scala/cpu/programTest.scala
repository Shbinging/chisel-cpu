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


class program_test(c: program) extends PeekPokeTester(c){
poke(c.io.init.writeEn, 1L.U)
step(1)
//addi $1, $0, 0
poke(c.io.init.addr, 0L.U);poke(c.io.init.instr, 536936448L.U);step(1)
//addi $2, $0, 1
poke(c.io.init.addr, 1L.U);poke(c.io.init.instr, 537001985L.U);step(1)
//addi $4, $0, 10
poke(c.io.init.addr, 2L.U);poke(c.io.init.instr, 537133066L.U);step(1)
//slti $5, $4, 3
poke(c.io.init.addr, 3L.U);poke(c.io.init.instr, 679804931L.U);step(1)
//bne $5, $0, 7
poke(c.io.init.addr, 4L.U);poke(c.io.init.instr, 335872007L.U);step(1)
//addi $6, $4, 65533
poke(c.io.init.addr, 5L.U);poke(c.io.init.instr, 545718269L.U);step(1)
//bltz $0, $6, 10
poke(c.io.init.addr, 6L.U);poke(c.io.init.instr, 79691786L.U);step(1)
//add $7, $1, $2, 0
poke(c.io.init.addr, 7L.U);poke(c.io.init.instr, 2242592L.U);step(1)
//addi $1, $2, 0
poke(c.io.init.addr, 8L.U);poke(c.io.init.instr, 541130752L.U);step(1)
//addi $2, $7, 0
poke(c.io.init.addr, 9L.U);poke(c.io.init.instr, 551682048L.U);step(1)
//addi $6, $6, 65535
poke(c.io.init.addr, 10L.U);poke(c.io.init.instr, 549912575L.U);step(1)
//beq $0, $0, 65530
poke(c.io.init.addr, 11L.U);poke(c.io.init.instr, 268500986L.U);step(1)
//addi $5, $4, 65534
poke(c.io.init.addr, 12L.U);poke(c.io.init.instr, 545652734L.U);step(1)
//bltz $0, $5, 2
poke(c.io.init.addr, 13L.U);poke(c.io.init.instr, 77594626L.U);step(1)
//addi $2, $0, 1
poke(c.io.init.addr, 14L.U);poke(c.io.init.instr, 537001985L.U);step(1)
//beq $0, $0, 1
poke(c.io.init.addr, 15L.U);poke(c.io.init.instr, 268435457L.U);step(1)
//addi $2, $0, 0
poke(c.io.init.addr, 16L.U);poke(c.io.init.instr, 537001984L.U);step(1)
//halt 0
poke(c.io.init.addr, 17L.U);poke(c.io.init.instr, 4227858432L.U);step(1)

//set pc to 0
poke(c.io.init.pcInit, 0.U)
poke(c.io.init.cpuReset, 1.U)
step(1)
poke(c.io.init.cpuReset, 0.U)
        step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);step(1);

expect(c.io.watch.regs(2), 34.U)
}

object programGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new program) {c => new program_test(c)}
}