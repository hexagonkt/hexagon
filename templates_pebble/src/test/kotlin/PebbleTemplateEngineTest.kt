package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplateEngineTest

internal class PebbleTemplateEngineTest :
    TemplateEngineTest("templates/test.pebble.html", { PebbleAdapter })