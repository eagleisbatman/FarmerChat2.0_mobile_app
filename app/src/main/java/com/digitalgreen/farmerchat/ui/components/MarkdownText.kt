package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: Int = 15,
    lineHeight: Int = 22
) {
    val formattedText = parseMarkdown(text, color, fontSize)
    
    SelectionContainer {
        Text(
            text = formattedText,
            modifier = modifier,
            lineHeight = lineHeight.sp
        )
    }
}

private fun parseMarkdown(text: String, textColor: Color, baseFontSize: Int): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val lines = text.split('\n')
        
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append('\n')
            
            when {
                // Headers
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 2).sp,
                        color = textColor
                    )) {
                        append(line.substring(4))
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 4).sp,
                        color = textColor
                    )) {
                        append(line.substring(3))
                    }
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 6).sp,
                        color = textColor
                    )) {
                        append(line.substring(2))
                    }
                }
                // Bullet points
                line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                    append("  • ")
                    processInlineMarkdown(line.trim().substring(2), textColor, baseFontSize)
                }
                // Numbered lists
                line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val parts = line.trim().split(Regex("^\\d+\\.\\s"), 2)
                    val number = line.trim().substringBefore(".")
                    append("  $number. ")
                    if (parts.size > 1) {
                        processInlineMarkdown(parts[1], textColor, baseFontSize)
                    }
                }
                // Regular text
                else -> {
                    processInlineMarkdown(line, textColor, baseFontSize)
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.processInlineMarkdown(
    text: String,
    textColor: Color,
    baseFontSize: Int
) {
    var currentText = text
    var lastIndex = 0
    
    // Process bold (**text**)
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    val boldMatches = boldRegex.findAll(currentText)
    
    boldMatches.forEach { match ->
        if (match.range.first > lastIndex) {
            append(currentText.substring(lastIndex, match.range.first))
        }
        withStyle(SpanStyle(
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = baseFontSize.sp
        )) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    
    if (lastIndex < currentText.length) {
        // Process italic (*text*)
        val remaining = currentText.substring(lastIndex)
        var processedText = remaining
        
        val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")
        processedText = processedText.replace(italicRegex) { matchResult ->
            buildAnnotatedString {
                withStyle(SpanStyle(
                    fontStyle = FontStyle.Italic,
                    color = textColor,
                    fontSize = baseFontSize.sp
                )) {
                    append(matchResult.groupValues[1])
                }
            }.toString()
        }
        
        // Process inline code (`code`)
        val codeRegex = Regex("`(.+?)`")
        val parts = processedText.split(codeRegex)
        val matches = codeRegex.findAll(processedText).toList()
        
        parts.forEachIndexed { index, part ->
            if (index > 0 && index <= matches.size) {
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = (baseFontSize - 1).sp,
                    background = Color.Gray.copy(alpha = 0.2f),
                    color = textColor
                )) {
                    append(matches[index - 1].groupValues[1])
                }
            }
            withStyle(SpanStyle(
                color = textColor,
                fontSize = baseFontSize.sp
            )) {
                append(part)
            }
        }
    }
}