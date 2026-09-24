package com.erns.alertauni.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.erns.alertauni.R


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        //fontFamily = FontFamily.Default,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val TPSerif = FontFamily(
    Font(R.font.ptserif_regular, FontWeight.Normal),
    Font(R.font.ptserif_bold, FontWeight.Bold),
    Font(R.font.ptserif_italic, FontWeight.Normal, FontStyle.Italic)
)

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = TPSerif,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = TPSerif,
        fontSize = 22.sp
    )
)