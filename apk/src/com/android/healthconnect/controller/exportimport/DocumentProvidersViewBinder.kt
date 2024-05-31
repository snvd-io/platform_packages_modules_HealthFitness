/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.healthconnect.controller.exportimport

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.exportimport.api.DocumentProvider
import com.android.healthconnect.controller.exportimport.api.DocumentProviderRoot

/** Adds the views for the documents provider list when setting up export and import. */
class DocumentProvidersViewBinder {
    fun bindDocumentProvidersView(
        documentProviders: List<DocumentProvider>,
        documentProvidersView: ViewGroup,
        inflater: LayoutInflater,
        onSelectionChanged: (root: DocumentProviderRoot) -> Unit
    ) {
        for (documentProvider in documentProviders) {
            val documentProviderView =
                inflater.inflate(R.layout.item_document_provider, documentProvidersView, false)

            val radioButtonView =
                documentProviderView.findViewById<RadioButton>(
                    R.id.item_document_provider_radio_button)
            val iconView =
                documentProviderView.findViewById<ImageView>(R.id.item_document_provider_icon)
            val titleView =
                documentProviderView.findViewById<TextView>(R.id.item_document_provider_title)
            val summaryView =
                documentProviderView.findViewById<TextView>(R.id.item_document_provider_summary)

            iconView.setImageDrawable(
                loadPackageIcon(
                    documentProvidersView.context,
                    documentProvider.info.authority,
                    documentProvider.info.iconResource))
            titleView.setText(documentProvider.info.title)

            if (documentProvider.roots.size == 1) {
                val root = documentProvider.roots[0]

                summaryView.setText(root.summary)
                summaryView.setVisibility(VISIBLE)

                documentProviderView.setOnClickListener {
                    uncheckRadioButtons(documentProvidersView)
                    radioButtonView.setChecked(true)

                    onSelectionChanged(root)
                }

                // TODO: b/339189778 - Handle multiple document provider roots.
            } else {
                summaryView.setText("")
                summaryView.setVisibility(GONE)
            }

            documentProvidersView.addView(documentProviderView)
        }
    }

    private fun loadPackageIcon(context: Context, authority: String, icon: Int): Drawable? {
        val info = context.packageManager.resolveContentProvider(authority, 0)
        if (info != null) {
            return context.packageManager.getDrawable(info.packageName, icon, info.applicationInfo)
        }

        return null
    }

    private fun uncheckRadioButtons(view: ViewGroup) {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            val childRadioButton =
                child.findViewById<RadioButton>(R.id.item_document_provider_radio_button)
            childRadioButton?.setChecked(false)
        }
    }
}
