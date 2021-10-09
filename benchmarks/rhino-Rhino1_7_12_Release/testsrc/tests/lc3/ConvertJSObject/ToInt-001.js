/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

gTestfile = 'ToInt-001.js';

/**
 *  Preferred Argument Conversion.
 *
 *  Passing a JavaScript boolean to a Java method should prefer to call
 *  a Java method of the same name that expects a Java boolean.
 *
 */
var SECTION = "Preferred argument conversion:  JavaScript Object to int";
var VERSION = "1_4";
var TITLE   = "LiveConnect 3.0 JavaScript to Java Data Type Conversion " +
  SECTION;
startTest();

var TEST_CLASS = new Packages.com.netscape.javascript.qa.lc3.jsobject.JSObject_007;

new TestCase(
  "TEST_CLASS.ambiguous( new String() ) +''",
  "INT",
  TEST_CLASS.ambiguous(new String()) +'' );

new TestCase(
  "TEST_CLASS.ambiguous( new Boolean() ) +''",
  "INT",
  TEST_CLASS.ambiguous( new Boolean() )+'' );

new TestCase(
  "TEST_CLASS.ambiguous( new Number() ) +''",
  "INT",
  TEST_CLASS.ambiguous( new Number() )+'' );

new TestCase(
  "TEST_CLASS.ambiguous( new Date(0) ) +''",
  "INT",
  TEST_CLASS.ambiguous( new Date(0) )+'' );

test();


