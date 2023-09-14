package dev.apollo.luckynetwork.globalplayerlookup.datalayer;

import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.repository.PlayerRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SQLDriver {

    public Optional<LookedPlayer> getPlayer(Connection connection, UUID uuid) throws SQLException
    {
        String SQL = "SELECT `uuid`, `name`, `firstjoin`, `lastjoin`, `premium` FROM `playerdata` WHERE `uuid` = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL))
        {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    LookedPlayer player = new LookedPlayer(UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("name"),
                            resultSet.getTimestamp("firstjoin").toLocalDateTime(),
                            resultSet.getTimestamp("lastjoin").toLocalDateTime(),
                            resultSet.getBoolean("premium"));
                    return Optional.of(player);
                }
                return Optional.empty();
            }
        }
    }

    public Optional<LookedPlayer> getPlayer(Connection connection, String name) throws SQLException
    {
        String SQL = "SELECT `uuid`, `name`, `firstjoin`, `lastjoin`, `premium` FROM `playerdata` WHERE `name` = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL))
        {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    LookedPlayer player = new LookedPlayer(UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("name"),
                            resultSet.getTimestamp("firstjoin").toLocalDateTime(),
                            resultSet.getTimestamp("lastjoin").toLocalDateTime(),
                            resultSet.getBoolean("premium"));

                    return Optional.of(player);
                }

                return Optional.empty();
            }
        }
    }

}
