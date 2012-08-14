package de.htwg_konstanz.in.jca;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestThreeValueBoolean {

    @Test
    public void testAndThreeValueBoolean() {
	assertEquals(ThreeValueBoolean.no.and(ThreeValueBoolean.no), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.no.and(ThreeValueBoolean.unknown), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.no.and(ThreeValueBoolean.yes), ThreeValueBoolean.no);

	assertEquals(ThreeValueBoolean.unknown.and(ThreeValueBoolean.no), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.unknown.and(ThreeValueBoolean.unknown), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.unknown.and(ThreeValueBoolean.yes), ThreeValueBoolean.unknown);

	assertEquals(ThreeValueBoolean.yes.and(ThreeValueBoolean.no), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.yes.and(ThreeValueBoolean.unknown), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.yes.and(ThreeValueBoolean.yes), ThreeValueBoolean.yes);
    }

    @Test
    public void testAndBoolean() {
	assertEquals(ThreeValueBoolean.no.and(false), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.no.and(true), ThreeValueBoolean.no);

	assertEquals(ThreeValueBoolean.unknown.and(false), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.unknown.and(true), ThreeValueBoolean.unknown);

	assertEquals(ThreeValueBoolean.yes.and(false), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.yes.and(true), ThreeValueBoolean.yes);
    }

    @Test
    public void testOrThreeValueBoolean() {
	assertEquals(ThreeValueBoolean.no.or(ThreeValueBoolean.no), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.no.or(ThreeValueBoolean.unknown), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.no.or(ThreeValueBoolean.yes), ThreeValueBoolean.yes);

	assertEquals(ThreeValueBoolean.unknown.or(ThreeValueBoolean.no), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.unknown.or(ThreeValueBoolean.unknown), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.unknown.or(ThreeValueBoolean.yes), ThreeValueBoolean.yes);

	assertEquals(ThreeValueBoolean.yes.or(ThreeValueBoolean.no), ThreeValueBoolean.yes);
	assertEquals(ThreeValueBoolean.yes.or(ThreeValueBoolean.unknown), ThreeValueBoolean.yes);
	assertEquals(ThreeValueBoolean.yes.or(ThreeValueBoolean.yes), ThreeValueBoolean.yes);
    }

    @Test
    public void testOrBoolean() {
	assertEquals(ThreeValueBoolean.no.or(false), ThreeValueBoolean.no);
	assertEquals(ThreeValueBoolean.no.or(true), ThreeValueBoolean.yes);

	assertEquals(ThreeValueBoolean.unknown.or(false), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.unknown.or(true), ThreeValueBoolean.yes);

	assertEquals(ThreeValueBoolean.yes.or(false), ThreeValueBoolean.yes);
	assertEquals(ThreeValueBoolean.yes.or(true), ThreeValueBoolean.yes);
    }

    @Test
    public void testNot() {
	assertEquals(ThreeValueBoolean.no.not(), ThreeValueBoolean.yes);
	assertEquals(ThreeValueBoolean.unknown.not(), ThreeValueBoolean.unknown);
	assertEquals(ThreeValueBoolean.yes.not(), ThreeValueBoolean.no);
    }

    @Test
    public void testFromBoolean() {
	assertEquals(ThreeValueBoolean.fromBoolean(true), ThreeValueBoolean.yes);
	assertEquals(ThreeValueBoolean.fromBoolean(false), ThreeValueBoolean.no);
    }
}
