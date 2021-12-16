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
class aluTest extends FreeSpec with ChiselScalatestTester {
    "Test alu" in {
    test(new Alu){c =>
        c.io.ALU_op.poke("b1010".U)
        c.io.A_in.poke(0xfffffff8L.U)
        c.io.B_in.poke(0x8f.U)
        c.clock.step(1)
    }
}
}

class ALU_test(c: Alu) extends PeekPokeTester(c){
    val nums:Array[Long] = Array(0x80000000L, 0x80000001L, 0xFFFFFFFEL, 0xFFFFFFFFL, 0, 1, 2, 0x7FFFFFFEL, 0x7FFFFFFFL)
    def AddSubTestNoOverFlow(c: Alu){
        for(a <- nums) {
            for(b <- nums){
                poke(c.io.A_in, a)
                poke(c.io.B_in, b)
                poke(c.io.ALU_op, "b0000".U)//ADD
                step(1)
                expect(c.io.Zero, (((a + b) & 0xFFFFFFFFL) == 0).B)
                expect(c.io.Overflow_out, false.B)
                expect(c.io.ALU_out, (a + b) & 0xFFFFFFFFL)
                step(1)
                
                poke(c.io.A_in, a)
                poke(c.io.B_in, b)
                poke(c.io.ALU_op, "b0001".U)//SUB
                step(1)
                expect(c.io.Zero, (((a - b) & 0xFFFFFFFFL) == 0L).B)
                expect(c.io.Overflow_out, false.B)
                expect(c.io.ALU_out, (a - b) & 0xFFFFFFFFL)
                step(1)
            }
        }    
    }

    def leadingZeroOneTest(c: Alu){
            poke(c.io.A_in, "h_0040_0000".U)
            poke(c.io.ALU_op, "b0010".U)
            expect(c.io.ALU_out, 9.U)
            step(1)// leading zero
            
            poke(c.io.A_in, "h_ff11_1111".U)
            poke(c.io.ALU_op, 3.U)
            expect(c.io.ALU_out, 8.U)// leading one
            step(1)
    }

    def BitWiseOperationTest(c: Alu){
        val randNum = new Random
        // Test AND
        for(i <- 1 until 100){
            val a = randNum.nextInt(0x7FFFFFFF)
            val b = randNum.nextInt(0x7FFFFFFF)
            poke(c.io.A_in, a)
            poke(c.io.B_in, b)
            poke(c.io.ALU_op, "b0100".U)//AND
            expect(c.io.ALU_out, a & b)
            step(1)
        }
        // Test OR
        for(i <- 1 until 100){
            val a = randNum.nextInt(0x7FFFFFFF)
            val b = randNum.nextInt(0x7FFFFFFF)
            poke(c.io.A_in, a)
            poke(c.io.B_in, b)
            poke(c.io.ALU_op, "b0110".U)
            expect(c.io.ALU_out, a | b)
            step(1)
        }
        // Test orNot
        for(i <- 1 until 2){
            val a = randNum.nextInt(0x7FFFFFFF)
            val b = randNum.nextInt(0x7FFFFFFF)
            poke(c.io.A_in, a)
            poke(c.io.B_in, b)
            poke(c.io.ALU_op, "b1000".U)//ADD
            //scala.Predef.println(a, b)
            expect(c.io.ALU_out, (~(a | b)) & 0xffffffffL)
            step(1)
        }
    }

    def CompareTest(c: Alu){
        val randNum = new Random
        // Test SLT, SLTI
        for(i <- 1 until 500){
            val a = randNum.nextInt(0x7FFFFFFF)
            val b = randNum.nextInt(0x7FFFFFFF)
            poke(c.io.A_in, a)
            poke(c.io.B_in, b)
            poke(c.io.ALU_op, "b0101".U)//ADD
            val less = (a < b)
            expect(c.io.Less, less)
            expect(c.io.ALU_out, less)
            expect(c.io.Overflow_out, 0.U)
            step(1)
        }
        // Test SLTU, SLTIU
        for(i <- 1 until 500){
            val a = randNum.nextInt(0x7FFFFFFF)
            val b = randNum.nextInt(0x7FFFFFFF)
            poke(c.io.A_in, a)
            poke(c.io.B_in, b)
            poke(c.io.ALU_op, "b0111".U)//ADD
            val less = ((a & 0xFFFFFFFEL) < (b & 0xFFFFFFFEL))
            expect(c.io.Less, less)
            expect(c.io.ALU_out, less)
            expect(c.io.Overflow_out, 0.U)
            step(1)
        }
    }

    def seBHTest(c:Alu) = {
        poke(c.io.ALU_op, "b1010".U)
        poke(c.io.B_in, 0x1f.U)
        expect(c.io.ALU_out, 0x1f.U)
        expect(c.io.Overflow_out, 0.U)
        step(1)

        poke(c.io.ALU_op, "b1010".U)
        poke(c.io.B_in, 0xf1.U)
        expect(c.io.ALU_out, 0xfffffff1L.U)
        expect(c.io.Overflow_out, 0.U)
        step(1)

        poke(c.io.ALU_op, "b1011".U)
        poke(c.io.B_in, 0x8cf1.U)
        expect(c.io.ALU_out, 0xffff8cf1L.U)
        expect(c.io.Overflow_out, 0.U)
        step(1)

        poke(c.io.ALU_op, "b1011".U)
        poke(c.io.B_in, 0x7cf1.U)
        expect(c.io.ALU_out, 0x7cf1.U)
        expect(c.io.Overflow_out, 0.U)
        step(1)
    }
    
    def getSign(c: Long):Int  = {
        if ((c & 0xFFFFFFFFL).toInt < 0)
            -1
        else 
            1
    }
    def AddSubTestOverFlow(c: Alu){
        for(a <- nums) {
            for(b <- nums){
                poke(c.io.A_in, a)
                poke(c.io.B_in, b)
                poke(c.io.ALU_op, "b1110".U)//ADD
                expect(c.io.Zero, (((a + b) & 0xFFFFFFFFL) == 0).B)
                expect(c.io.Overflow_out, ((getSign(a) == getSign(b)) & (getSign(b) != getSign(1L*a + b))).B)
                expect(c.io.ALU_out, (a + b) & 0xFFFFFFFFL)
                step(1)
                
                poke(c.io.A_in, a)
                poke(c.io.B_in, b)
                poke(c.io.ALU_op, "b1111".U)//ADD
                expect(c.io.Zero, (((a - b) & 0xFFFFFFFFL) == 0).B)
                expect(c.io.Less, (a.toInt < b.toInt).B)
                expect(c.io.Overflow_out, ((getSign(a) != getSign(b)) & (getSign(a) != getSign(1L*a - b))).B)
                expect(c.io.ALU_out, (a - b) & 0xFFFFFFFFL)
                step(1)
            }
        }    
    }
    AddSubTestNoOverFlow(c)   
    leadingZeroOneTest(c)
    BitWiseOperationTest(c)
    CompareTest(c)
    seBHTest(c)
    AddSubTestOverFlow(c)
}


object ALUGen extends App{
  chisel3.iotesters.Driver.execute(args, () => new Alu) {c => new ALU_test(c)}
}
