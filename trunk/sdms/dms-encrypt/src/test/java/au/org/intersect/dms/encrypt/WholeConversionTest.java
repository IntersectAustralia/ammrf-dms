/**
 * Project: Platforms for Collaboration at the AMMRF
 *
 * Copyright (c) Intersect Pty Ltd, 2011
 *
 * @see http://www.ammrf.org.au
 * @see http://www.intersect.org.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * This program contains open source third party libraries from a number of
 * sources, please read the THIRD_PARTY.txt file for more details.
 */

package au.org.intersect.dms.encrypt;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import au.org.intersect.dms.tunnel.HddUtil;


public class WholeConversionTest
{
    
    @Test
    public void TestingWholeConversion()
    {
        byte[] testBytes = {1, 5, 94, 35, 4, 0, 78};
        
        String hexString = HddUtil.convertByteToHexString(testBytes);
        byte[] convertedBackToBytes = HddUtil.convertHexToByteArray(hexString);
        ReflectionAssert.assertReflectionEquals(convertedBackToBytes, testBytes);
    }
    
    @Test
    public void TestingWholeConversionForBigInput()
    {
        byte[] testBytes = new byte[1024];
        for (int i = 0; i < testBytes.length; i++)
        {
            testBytes[i] = (byte) (i * 7);
        }
        
        String hexString = HddUtil.convertByteToHexString(testBytes);
        byte[] convertedBackToBytes = HddUtil.convertHexToByteArray(hexString);
        ReflectionAssert.assertReflectionEquals(convertedBackToBytes, testBytes);
    }
    
    

}
