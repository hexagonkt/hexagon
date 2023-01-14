package com.hexagonkt.core

import java.net.ServerSocket
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.URL
import kotlin.test.*

internal class NetworkTest {

    // TODO Replace URL accesses by local started HTTP servers
    @Test fun `Check URL exists`() {
        assert(URL("http://example.com").exists())
        assert(URL("https://example.com").exists())
        assert(URL("file:README.md").exists())
        assert(URL("file:src/test/resources/build.properties").exists())
        assert(URL("classpath:locales/data.json").exists())

        assert(!URL("http://example.com/a.txt").exists())
        assert(!URL("https://example.com/b.html").exists())
        assert(!URL("file:not_existing.txt").exists())
        assert(!URL("file:src").exists())
        assert(!URL("classpath:data.json").exists())
    }

    @Test fun `Check URL first variant`() {
        URL("classpath:locales/data.json").let {
            assertEquals(it, it.firstVariant("_it_IT", "_it"))
        }
        assertEquals(
            URL("classpath:locales/data_en_US.json"),
            URL("classpath:locales/data.json").firstVariant("_en_US", "_en"),
        )
        assertEquals(
            URL("classpath:locales/data_en.json"),
            URL("classpath:locales/data.json").firstVariant("_en_GB", "_en"),
        )
    }

    @Test fun `Check localized URL`() {
        URL("classpath:locales/data.json").let {
            assertEquals(it, it.localized(parseLocale("it_IT")))
        }
        assertEquals(
            URL("classpath:locales/data_en_US.json"),
            URL("classpath:locales/data.json").localized(parseLocale("en_US")),
        )
        assertEquals(
            URL("classpath:locales/data_en.json"),
            URL("classpath:locales/data.json").localized(parseLocale("en_GB")),
        )
    }

    @Test fun `Internet address helper works correctly`() {
        assertEquals(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), allInterfaces)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1)), loopbackInterface)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 3, 2, 1)), inetAddress(127, 3, 2, 1))
    }

    @Test fun `Network ports utilities work properly`() {
        assert(!isPortOpened(freePort()))
        ServerSocket(0).use {
            assert(isPortOpened(it.localPort))
        }
    }

    @Test fun `URL check works properly`() {
        assertTrue { URL("http://example.com").responseSuccessful() }
        assertFalse { URL("http://invalid-domain.z").responseSuccessful() }
        assertTrue { URL("http://example.com").responseFound() }
        assertFalse { URL("http://example.com/nothing").responseFound() }
    }

    @Test fun `Properties can be loaded from URLs`() {
        val properties = properties(URL("classpath:build.properties"))
        assertEquals("hexagon", properties["project"])
        assertEquals("core", properties["module"])
        assertEquals("1.0.0", properties["version"])
        assertEquals("com.hexagonkt", properties["group"])
        assertEquals("Hexagon Toolkit", properties["description"])
        assertEquals("1", properties["number"])
        assertNull(properties["invalid"])

        assertFailsWith<ResourceNotFoundException> {
            properties(URL("classpath:invalid.properties"))
        }
    }

    @Test fun `Production mode is disabled by default`() {
        assertFalse(disableChecks)
    }

    @Test fun `Check multiple errors`() {
        val e = assertFailsWith<MultipleException> {
            check(
                "Test multiple exceptions",
                { require(false) { "Sample error" } },
                { println("Good block")},
                { error("Bad state") },
            )
        }

        assertEquals("Test multiple exceptions", e.message)
        assertEquals(2, e.causes.size)
        assertEquals("Sample error", e.causes[0].message)
        assertEquals("Bad state", e.causes[1].message)

        check(
            "No exception thrown",
            { println("Good block")},
            { println("Shouldn't throw an exception")},
        )
    }

    @Test fun `Print helper`() {
        assertEquals("text\n", "echo text".exec().println("command output: "))
        assertEquals("text\n", "echo text".exec().println())
        assertEquals(null, null.println())
        assertEquals("text", "text".println())
    }

    @Test fun `Process execution works as expected`() {
        assertFailsWith<IllegalArgumentException> { " ".exec() }
        assertFailsWith<IllegalArgumentException> { "echo test".exec(timeout = -1) }
        assertFailsWith<IllegalArgumentException> { "echo test".exec(timeout = 0) }
        assertFailsWith<IllegalStateException> { "sleep 2".exec(timeout = 1) }
        assertFailsWith<CodedException> { "false".exec(fail = true) }

        assert("false".exec().isEmpty())
        assert("sleep 1".exec().isEmpty())
        assertEquals("str\n", "echo str".exec())
    }

    @Test fun `System setting works ok` () {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting<String>("system_property") == "value")

        assert(Jvm.systemSetting<String>("PATH").isNotEmpty())
        assertNull(Jvm.systemSettingOrNull<String>("_not_defined_"))

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") == "path override")
    }

    @Test fun `Filtering an exception with an empty string do not change the stack` () {
        val t = RuntimeException ()
        assert (t.stackTrace?.contentEquals(t.filterStackTrace ("")) ?: false)
    }

    @Test fun `Filtering an exception with a package only returns frames of that package` () {
        val t = RuntimeException ()
        t.filterStackTrace ("com.hexagonkt.core").forEach {
            assert (it.className.startsWith ("com.hexagonkt.core"))
        }
    }

    @Test fun `'fail' generates the correct exception`() {
        assertFailsWith<IllegalStateException>("Invalid state") {
            fail
        }
    }

    @Test fun `Printing an exception returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error")
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${NetworkTest::class.java.name}"))
    }

    @Test fun `Printing an exception with a cause returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error", IllegalStateException ("invalid state"))
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${NetworkTest::class.java.name}"))
    }

    @Test fun `Multiple retry errors throw an exception` () {
        val retries = 3
        try {
            retry(retries, 1) { throw RuntimeException ("Retry error") }
        }
        catch (e: MultipleException) {
            assert (e.causes.size == retries)
        }
    }

    @Test fun `Retry does not allow invalid parameters` () {
        assertFailsWith<IllegalArgumentException> { retry(0, 1) {} }
        assertFailsWith<IllegalArgumentException> { retry(1, -1) {} }
        retry(1, 0) {} // Ok case
    }
}