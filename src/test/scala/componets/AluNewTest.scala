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

class ALU_test1(c: AluNew) extends PeekPokeTester(c){
    poke(c.io.ALU_op, "b1110".U)
    poke(c.io.A_in, 6.U)
    poke(c.io.B_in, 8.U)
    print(peek(c.io.ALU_out).toString() + "\n")
}
object ALUGen1 extends App{
  chisel3.iotesters.Driver.execute(args, () => new AluNew) {c => new ALU_test1(c)}
}
