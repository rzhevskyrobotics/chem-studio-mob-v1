package com.example.smart_labs.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smart_labs.domain.catalog.TopicCatalog
import com.example.smart_labs.domain.model.TopicItem
import com.example.smart_labs.domain.model.TopicKind
import com.example.smart_labs.domain.search.NGramSearch
import com.example.smart_labs.ui.components.AppHeader

@Composable
fun TopicCatalogScreen(
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val all = remember { TopicCatalog.items }
    val filtered = remember(query) { NGramSearch.search(all, query) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AppHeader(showScreenTitle = true, screenTitle = "Справочник топиков")

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onBack) { Text("Назад") }
                Text(
                    text = "${filtered.size}/${all.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Поиск") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    TopicItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun TopicItemCard(item: TopicItem) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(item.topic, style = MaterialTheme.typography.bodySmall)

            item.description?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = { },
                label = {
                    Text(if (item.kind == TopicKind.SENSOR) "SENSOR" else "CONTROL")
                }
            )
        }
    }
}