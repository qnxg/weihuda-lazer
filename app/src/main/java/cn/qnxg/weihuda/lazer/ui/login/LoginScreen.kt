// weihuda!lazer
// Copyright 2026 weihuda!lazer contributors. All rights reserved.
// SPDX-License-Identifier: MIT OR Apache-2.0

package cn.qnxg.weihuda.lazer.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.qnxg.weihuda.lazer.R
import cn.qnxg.weihuda.lazer.ui.theme.LazerColors

/**
 * 登录页：学号 + 密码换令牌。
 *
 * 表单只负责收集与基本校验，真正的失败文案由用例返回——教务的报错（密码错、需要二次验证）
 * 只有后端知道，界面不该自己猜。
 */
@Composable
fun LoginScreen(
	submitting: Boolean,
	error: String?,
	onBack: () -> Unit,
	onForgotPassword: () -> Unit,
	onSubmit: (String, String) -> Unit,
) {
	var stuId by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var revealPassword by remember { mutableStateOf(false) }

	val canSubmit = stuId.isNotBlank() && password.isNotEmpty() && !submitting
	val submit = { if (canSubmit) onSubmit(stuId, password) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(LazerColors.PageBackground)
			.statusBarsPadding()
			.imePadding()
			.verticalScroll(rememberScrollState()),
	) {
		IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
			Icon(
				painter = painterResource(R.drawable.ic_arrow_back),
				contentDescription = "返回",
				tint = LazerColors.Ink,
				modifier = Modifier.size(24.dp),
			)
		}

		Column(
			modifier = Modifier.padding(horizontal = 24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Spacer(Modifier.height(24.dp))
			Text(
				text = "登录校园账号",
				fontSize = 32.sp,
				fontWeight = FontWeight.Bold,
				color = LazerColors.Ink,
				textAlign = TextAlign.Center,
				lineHeight = 40.sp,
			)
			Spacer(Modifier.height(10.dp))
			Text(
				text = "登录后即可使用所有组件",
				fontSize = 14.sp,
				color = LazerColors.InkMuted,
				textAlign = TextAlign.Center,
			)
			Spacer(Modifier.height(32.dp))

			LoginField(
				value = stuId,
				onValueChange = { stuId = it.trim() },
				hint = "学号",
				icon = R.drawable.ic_person,
				keyboardType = KeyboardType.Number,
			)
			Spacer(Modifier.height(14.dp))
			LoginField(
				value = password,
				onValueChange = { password = it },
				hint = "密码",
				icon = R.drawable.ic_lock,
				keyboardType = KeyboardType.Password,
				imeAction = ImeAction.Done,
				visualTransformation = if (revealPassword) {
					VisualTransformation.None
				} else {
					PasswordVisualTransformation()
				},
				trailing = {
					IconButton(onClick = { revealPassword = !revealPassword }) {
						Icon(
							painter = painterResource(
								if (revealPassword) R.drawable.ic_eye_off else R.drawable.ic_eye,
							),
							contentDescription = if (revealPassword) "隐藏密码" else "显示密码",
							tint = LazerColors.InkMuted,
							modifier = Modifier.size(20.dp),
						)
					}
				},
				onDone = submit,
			)

			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Text(
					text = "忘记密码？",
					fontSize = 13.sp,
					fontWeight = FontWeight.Medium,
					color = LazerColors.Brand,
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.clickable(onClick = onForgotPassword)
						.padding(horizontal = 4.dp, vertical = 10.dp),
				)
			}

			Spacer(Modifier.height(6.dp))
			Button(
				onClick = submit,
				enabled = canSubmit,
				shape = RoundedCornerShape(28.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = LazerColors.Brand,
					contentColor = Color.White,
					disabledContainerColor = LazerColors.Brand.copy(alpha = 0.35f),
					disabledContentColor = Color.White,
				),
				modifier = Modifier
					.fillMaxWidth()
					.height(54.dp),
			) {
				if (submitting) {
					CircularProgressIndicator(
						color = Color.White,
						strokeWidth = 2.dp,
						modifier = Modifier.size(20.dp),
					)
				} else {
					Text("登录", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
				}
			}

			if (error != null) {
				Spacer(Modifier.height(12.dp))
				Text(
					text = error,
					fontSize = 13.sp,
					color = LazerColors.Danger,
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth(),
				)
			}

			Spacer(Modifier.height(32.dp))
		}
	}
}

@Composable
private fun LoginField(
	value: String,
	onValueChange: (String) -> Unit,
	hint: String,
	icon: Int,
	keyboardType: KeyboardType,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	trailing: (@Composable () -> Unit)? = null,
	imeAction: ImeAction = ImeAction.Next,
	onDone: () -> Unit = {},
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		placeholder = { Text(hint, fontSize = 15.sp, color = LazerColors.Finished) },
		leadingIcon = {
			Icon(
				painter = painterResource(icon),
				contentDescription = null,
				tint = LazerColors.InkMuted,
				modifier = Modifier.size(20.dp),
			)
		},
		trailingIcon = trailing,
		singleLine = true,
		shape = RoundedCornerShape(28.dp),
		visualTransformation = visualTransformation,
		keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
		keyboardActions = KeyboardActions(onDone = { onDone() }),
		colors = OutlinedTextFieldDefaults.colors(
			focusedContainerColor = Color.White,
			unfocusedContainerColor = Color.White,
			focusedBorderColor = LazerColors.Brand,
			unfocusedBorderColor = LazerColors.Line,
			cursorColor = LazerColors.Brand,
			focusedTextColor = LazerColors.Ink,
			unfocusedTextColor = LazerColors.Ink,
		),
		modifier = Modifier.fillMaxWidth(),
	)
}

/** 头像占位：未登录时首页与登录页都用它，保证两处看起来是同一个人。 */
@Composable
fun SignedOutAvatar(size: Int = 48) {
	Box(
		modifier = Modifier
			.size(size.dp)
			.clip(CircleShape)
			.background(LazerColors.Line),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			painter = painterResource(R.drawable.ic_person),
			contentDescription = null,
			tint = LazerColors.InkMuted,
			modifier = Modifier.size((size * 0.55f).dp),
		)
	}
}
