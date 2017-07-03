package mars.mips.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;

/*
Copyright (c) 2017,  Benjamin Landers

Developed by Benjamin Landers (benjaminrlanders@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Helper class for 4 argument floating point instructions
 */
public abstract class FusedFloat extends BasicInstruction {
    public FusedFloat(String usage, String description, String op) {
        super(usage, description, BasicInstructionFormat.R4_FORMAT,
                "qqqqq 00 ttttt sssss " + Floating.ROUNDING_MODE + " fffff 100" + op + "11");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        float result = compute(Float.intBitsToFloat(Coprocessor1.getValue(operands[1])),
                Float.intBitsToFloat(Coprocessor1.getValue(operands[2])),
                Float.intBitsToFloat(Coprocessor1.getValue(operands[3])));
        if (Float.isNaN(result)) {
            Coprocessor0.orRegister("fcsr", 0x10); // Set invalid flag
        }
        if (Float.isInfinite(result)) {
            Coprocessor0.orRegister("fcsr", 0x4); // Set Overflow flag
        }
        if (Floating.subnormal(result)) {
            Coprocessor0.orRegister("fcsr", 0x2); // Set Underflow flag
        }
        Coprocessor1.setRegisterToFloat(operands[0], result);
    }

    /**
     * @param r1 The first register
     * @param r2 The second register
     * @param r3 The third register
     * @return The value to store to the destination
     */
    protected abstract float compute(float r1, float r2, float r3);
}