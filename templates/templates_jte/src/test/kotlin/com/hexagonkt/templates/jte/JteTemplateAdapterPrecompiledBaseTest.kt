package com.hexagonkt.templates.jte

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest

internal class JteTemplateAdapterPrecompiledBaseTest :
    TemplateAdapterTest(
        urlOf("classpath:test.jte"),
        JteAdapter(TEXT_HTML, urlOf("classpath:/"), precompiled = true)
    )
