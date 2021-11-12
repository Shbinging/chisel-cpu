package components

import chisel3._
import chisel3.util._
import chisel3.tester._
import chisel3.tester.RawTester.test
import scala.collection._

import org.scalatest.FreeSpec
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import chiseltest.testableRecord
class regFileTest extends FreeSpec with ChiselScalatestTester {
    "Test regFile" in {
    test(new regFile3){ c =>
        c.io.Rs_addr.poke(1.U)
        c.io.Rd_addr.poke(1.U)
        c.io.Rd_byte_w_en.poke(15.U)
        c.io.Rd_in.poke(3.U)
        c.clock.step(1)
        c.io.Rs_out.expect(3.U)
    }
}
}

class regTests(c: regFile) extends PeekPokeTester(c) {  
        def testInitValue={
            for(i <- 0 to 31){
                testRead(i.U, 0.U, 0)
                testRead(i.U, 0.U, 1)
            }
        }

        def testRead(addr:UInt, expVal:UInt, swi:Int = 0) ={
            if(swi == 0){
                poke(c.io.Rs_addr, addr)
                expect(c.io.Rs_out, expVal)
            }
            else{
                poke(c.io.Rt_addr, addr)
                expect(c.io.Rt_out, expVal)
            }
            step(1)
        }

        def testWrite(addr:UInt, mask:UInt, wriVal:UInt) ={
            poke(c.io.Rd_addr, addr)
            poke(c.io.Rd_in, wriVal)
            poke(c.io.Rd_byte_w_en, mask)
            step(1)
        }


        // testInitValue//init value

        testWrite(1.U, "b1110".U, "b100011000".U)//test mask 0001
        testRead(1.U, "b11000".U, 0) //0x18

        testWrite(1.U, "b1101".U, 0xffff.U)
        testRead(1.U , 0xff18.U) //0xff18

        testWrite(1.U, "b1011".U, 0x18ffff.U)
        testRead(1.U, 0x18ff18.U, 1) //0x18ff18

        testWrite(1.U, "b0000".U ,0xffffffffL.U) //0xffffffffL
        testRead(1.U, 0xffffffffL.U)

        testWrite(2.U, "b0000".U, 0xff.U)//test rs and rd at the same time
        testRead(1.U, 0xffffffffL.U)
        testRead(2.U, 0xff.U, 1)

        testWrite(0.U, "b0000".U, 0xff.U)//test for reg 0 it should always be 0
        testRead(0.U, 0.U)

        testWrite(1.U, 0.U, 0.U)
        testWrite(2.U, 0.U, 0.U)// set to init

        
        // val regVir = Array.fill(32)(0)
        // for (r <- 1 to 20){
        //     for(i<- 1 to 31){
        //         var mask = ((i + r) % 15)
        //         var value = (i * r)
        //         testWrite(i.U, mask.U, value.U) 

                
        //         var sPart = Array.tabulate(4)(n => regVir(i) & (0xff << (n * 8)))
        //         var vPart = Array.tabulate(4)(n => value & (0xff << (n * 8)))
        //         var ans = 0
        //         for(j <- 0 to 3){
        //             if (((mask >> j) & 0x1) == 0){
        //                 ans += vPart(j)
        //             }else
        //                 ans += sPart(j)
        //         }
        //         //scala.Predef.println(ans)
        //         regVir(i) = ans
        //         testRead(i.U, ans.U)
        //     }
        // }
}
object RegGen extends App{
    chisel3.iotesters.Driver.execute(args, () => new regFile)(c => new regTests(c))
}