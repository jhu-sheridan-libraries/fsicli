package fsi.fsicli;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilTest {

	@Test
	public void testSHA256() {
		assertEquals("865fe0a59ebab91e6a7681a683a8463791e71385ffdbb746e2e65acffe762a05", Util.SHA256("cf12ffe48253f8831c9e4a4ee4ddc1283237b6c8517f81a252e9e1a746d72fa8d71c4cbeb9eaff1df0dfb1751ba760a97cce7694e464f7db5ca7546537e576a48f1565afa4a7372b6b64b4707cbf79c8b507beceb3be3a2e5ad66e0f9c10ae"));
	}
}
