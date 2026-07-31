package com.dexter.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dexter.app.R
import com.dexter.app.data.local.TeamMemberEntity

class TeamMemberAdapter(
    private val onRemoveClick: (TeamMemberEntity) -> Unit
) : ListAdapter<TeamMemberEntity, TeamMemberAdapter.TeamViewHolder>(TeamMemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team_member, parent, false)
        return TeamViewHolder(view, onRemoveClick)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TeamViewHolder(
        itemView: View,
        private val onRemove: (TeamMemberEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_team_pokemon_name)
        private val slotText: TextView = itemView.findViewById(R.id.text_team_slot)

        fun bind(member: TeamMemberEntity) {
            nameText.text = member.pokemonName.replaceFirstChar { it.uppercase() }
            slotText.text = "Slot ${member.slotPosition + 1}"
            itemView.setOnClickListener { onRemove(member) }
        }
    }

    private class TeamMemberDiffCallback : DiffUtil.ItemCallback<TeamMemberEntity>() {
        override fun areItemsTheSame(oldItem: TeamMemberEntity, newItem: TeamMemberEntity): Boolean =
            oldItem.slotPosition == newItem.slotPosition

        override fun areContentsTheSame(oldItem: TeamMemberEntity, newItem: TeamMemberEntity): Boolean =
            oldItem == newItem
    }
}
