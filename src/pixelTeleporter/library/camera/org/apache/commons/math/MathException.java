/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pixelTeleporter.library.camera.org.apache.commons.math;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Base class for commons-math checked exceptions.
 * <p>
 * Supports nesting, emulating JDK 1.4 behavior if necessary.
 * </p>
 * <p>
 * 
 * @version $Revision: 620312 $ $Date: 2008-02-10 12:28:59 -0700 (Sun, 10 Feb
 *          2008) $
 */
public class MathException extends Exception {

	/** Serializable version identifier */
	private static final long serialVersionUID = -8602234299177097102L;

	/**
	 * Does JDK support nested exceptions?
	 */
	private static final boolean JDK_SUPPORTS_NESTED;

	static {
		boolean flag = false;
		try {
			Throwable.class.getDeclaredMethod("getCause", new Class[0]);
			flag = true;
		} catch (final NoSuchMethodException ex) {
			flag = false;
		}
		JDK_SUPPORTS_NESTED = flag;
	}

	/** Cache for resources bundle. */
	private static ResourceBundle cachedResources = null;

	/**
	 * Pattern used to build the message.
	 */
	private final String pattern;

	/**
	 * Arguments used to build the message.
	 */
	private final Object[] arguments;

	/**
	 * Root cause of the exception
	 */
	private final Throwable rootCause;

	/**
	 * Translate a string to a given locale.
	 * 
	 * @param s
	 *            string to translate
	 * @param locale
	 *            locale into which to translate the string
	 * @return translated string or original string for unsupported locales or
	 *         unknown strings
	 */
	private static String translate(final String s, final Locale locale) {
		try {
			if ((cachedResources == null)
					|| (!cachedResources.getLocale().equals(locale))) {
				// caching the resource bundle
				cachedResources = ResourceBundle.getBundle(
						"peasy.org.apache.commons.math.MessagesResources", locale);
			}

			if (cachedResources.getLocale().getLanguage().equals(locale.getLanguage())) {
				// the value of the resource is the translated string
				return cachedResources.getString(s);
			}

		} catch (final MissingResourceException mre) {
			// do nothing here
		}

		// the locale is not supported or the resource is unknown
		// don't translate and fall back to using the string as is
		return s;

	}

	/**
	 * Builds a message string by from a pattern and its arguments.
	 * 
	 * @param pattern
	 *            format specifier
	 * @param arguments
	 *            format arguments
	 * @param locale
	 *            Locale in which the message should be translated
	 * @return a message string
	 */
	private static String buildMessage(final String pattern, final Object[] arguments,
			final Locale locale) {
		// do it the hard way, for Java 1.3. compatibility
		final MessageFormat mf = new MessageFormat(translate(pattern, locale));
		mf.setLocale(locale);
		return mf.format(arguments);
	}

	/**
	 * Constructs a new <code>MathException</code> with no detail message.
	 */
	public MathException() {
		super();
		this.pattern = null;
		this.arguments = new Object[0];
		this.rootCause = null;
	}

	/**
	 * Constructs a new <code>MathException</code> with specified detail
	 * message.
	 * 
	 * @param msg
	 *            the error message.
	 * @deprecated as of 1.2, replaced by
	 *             {@link #MathException(String, Object[])}
	 */
	@Deprecated
	public MathException(final String msg) {
		super(msg);
		this.pattern = msg;
		this.arguments = new Object[0];
		this.rootCause = null;
	}

	/**
	 * Constructs a new <code>MathException</code> with specified formatted
	 * detail message. Message formatting is delegated to
	 * {@link java.text.MessageFormat}.
	 * 
	 * @param pattern
	 *            format specifier
	 * @param arguments
	 *            format arguments
	 */
	public MathException(final String pattern, final Object[] arguments) {
		super(buildMessage(pattern, arguments, Locale.US));
		this.pattern = pattern;
		this.arguments = arguments.clone();
		this.rootCause = null;
	}

	/**
	 * Constructs a new <code>MathException</code> with specified nested
	 * <code>Throwable</code> root cause.
	 * 
	 * @param rootCause
	 *            the exception or error that caused this exception to be
	 *            thrown.
	 */
	public MathException(final Throwable rootCause) {
		super((rootCause == null ? null : rootCause.getMessage()));
		this.pattern = getMessage();
		this.arguments = new Object[0];
		this.rootCause = rootCause;
	}

	/**
	 * Constructs a new <code>MathException</code> with specified detail message
	 * and nested <code>Throwable</code> root cause.
	 * 
	 * @param msg
	 *            the error message.
	 * @param rootCause
	 *            the exception or error that caused this exception to be
	 *            thrown.
	 * @deprecated as of 1.2, replaced by
	 *             {@link #MathException(String, Object[], Throwable)}
	 */
	@Deprecated
	public MathException(final String msg, final Throwable rootCause) {
		super(msg);
		this.pattern = msg;
		this.arguments = new Object[0];
		this.rootCause = rootCause;
	}

	/**
	 * Constructs a new <code>MathException</code> with specified formatted
	 * detail message and nested <code>Throwable</code> root cause. Message
	 * formatting is delegated to {@link java.text.MessageFormat}.
	 * 
	 * @param pattern
	 *            format specifier
	 * @param arguments
	 *            format arguments
	 * @param rootCause
	 *            the exception or error that caused this exception to be
	 *            thrown.
	 * @since 1.2
	 */
	public MathException(final String pattern, final Object[] arguments,
			final Throwable rootCause) {
		super(buildMessage(pattern, arguments, Locale.US));
		this.pattern = pattern;
		this.arguments = arguments.clone();
		this.rootCause = rootCause;
	}

	/**
	 * Gets the pattern used to build the message of this throwable.
	 * 
	 * @return the pattern used to build the message of this throwable
	 * @since 1.2
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Gets the arguments used to build the message of this throwable.
	 * 
	 * @return the arguments used to build the message of this throwable
	 * @since 1.2
	 */
	public Object[] getArguments() {
		return arguments.clone();
	}

	/**
	 * Gets the message in a specified locale.
	 * 
	 * @param locale
	 *            Locale in which the message should be translated
	 * 
	 * @return localized message
	 * @since 1.2
	 */
	public String getMessage(final Locale locale) {
		return (pattern == null) ? null : buildMessage(pattern, arguments, locale);
	}

	/**
	 * Gets the cause of this throwable.
	 * 
	 * @return the cause of this throwable, or <code>null</code>
	 */
	@Override
	public Throwable getCause() {
		return rootCause;
	}

	/**
	 * Prints the stack trace of this exception to the standard error stream.
	 */
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * Prints the stack trace of this exception to the specified stream.
	 * 
	 * @param out
	 *            the <code>PrintStream</code> to use for output
	 */
	@Override
	public void printStackTrace(final PrintStream out) {
		synchronized (out) {
			final PrintWriter pw = new PrintWriter(out, false);
			printStackTrace(pw);
			// Flush the PrintWriter before it's GC'ed.
			pw.flush();
		}
	}

	/**
	 * Prints the stack trace of this exception to the specified writer.
	 * 
	 * @param out
	 *            the <code>PrintWriter</code> to use for output
	 */
	@Override
	public void printStackTrace(final PrintWriter out) {
		synchronized (out) {
			super.printStackTrace(out);
			if (rootCause != null && JDK_SUPPORTS_NESTED == false) {
				out.print("Caused by: ");
				rootCause.printStackTrace(out);
			}
		}
	}

}
