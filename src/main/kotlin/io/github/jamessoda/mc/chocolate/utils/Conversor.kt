package io.github.jamessoda.mc.chocolate.utils

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.math.BigInteger


object Conversor {
    fun deserializeItemStack(data: String): ItemStack? {
        val inputStream = ByteArrayInputStream(BigInteger(data, 32).toByteArray())
        val dataInputStream = DataInputStream(inputStream)
        var itemStack: ItemStack? = null
        try {
            val nbtTagCompoundClass = getNMSClass("NBTTagCompound")
            val nmsItemStackClass = getNMSClass("ItemStack")
            val nbtTagCompound = getNMSClass("NBTCompressedStreamTools")!!.getMethod("a", DataInputStream::class.java).invoke(null, dataInputStream)
            //Object nbtTagCompound = getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInputStream.class).invoke(null, inputStream);
            val craftItemStack = nmsItemStackClass!!.getMethod("createStack", nbtTagCompoundClass).invoke(null, nbtTagCompound)
            itemStack = getOBClass("inventory.CraftItemStack")!!.getMethod("asBukkitCopy", nmsItemStackClass).invoke(null, craftItemStack) as ItemStack
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return itemStack
    }

    fun serializeItemStack(item: ItemStack): String? {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)
        try {
            val nbtTagCompoundClass = getNMSClass("NBTTagCompound")
            val nbtTagCompoundConstructor = nbtTagCompoundClass!!.getConstructor()
            val nbtTagCompound = nbtTagCompoundConstructor.newInstance()
            val nmsItemStack = getOBClass("inventory.CraftItemStack")!!.getMethod("asNMSCopy", ItemStack::class.java).invoke(null, item)
            getNMSClass("ItemStack")!!.getMethod("save", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound)
            getNMSClass("NBTCompressedStreamTools")!!.getMethod("a", nbtTagCompoundClass, DataOutput::class.java).invoke(null, nbtTagCompound, dataOutput as DataOutput)
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return BigInteger(1, outputStream.toByteArray()).toString(32)
    }

    private fun getNMSClass(name: String): Class<*>? {
        val version = Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).toTypedArray()[3]
        return try {
            Class.forName("net.minecraft.server.$version.$name")
        } catch (var3: ClassNotFoundException) {
            var3.printStackTrace()
            null
        }
    }

    private fun getOBClass(name: String): Class<*>? {
        val version = Bukkit.getServer().javaClass.getPackage().name.split("\\.".toRegex()).toTypedArray()[3]
        return try {
            Class.forName("org.bukkit.craftbukkit.$version.$name")
        } catch (var3: ClassNotFoundException) {
            var3.printStackTrace()
            null
        }
    }
}