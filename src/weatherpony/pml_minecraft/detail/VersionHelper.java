package weatherpony.pml_minecraft.detail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class VersionHelper {
	static final String regex1 = ".*[0-9]+\\.[0-9]+";
	static final String regex2 = ".*[0-9]+w[0-9]+[a-zA-Z]";
	//static final String clientClass = "net/minecraft/client/Minecraft.class";
		//the client class is obfuscated. got to try and find it
	static final String clientMainClass = "net/minecraft/client/main/Main.class";
	static final String serverClass = "net/minecraft/server/MinecraftServer.class";
	private static final int ROOTSIZE = 2;
	public VersionHelper(){
		ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		boolean client;
		String sidedClass;
		//Object clientClassURL = Launch.classLoader.findResource(clientClass);
		Method findResource;
		try{
			findResource = ClassLoader.class.getDeclaredMethod("findResource", String.class);
		}catch(NoSuchMethodException e){
			throw new Error(e);
		}catch(SecurityException e){
			throw new RuntimeException(e);
		}
		findResource.setAccessible(true);
		Object clientMainClassURL;
		try{
			clientMainClassURL = findResource.invoke(currentLoader, clientMainClass);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		if(clientMainClassURL == null){
			Object serverClassURL;
			try{
				serverClassURL = findResource.invoke(currentLoader, serverClass);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
			if(serverClassURL == null){
				throw new RuntimeException();
			}else{
				client = false;
				sidedClass = serverClass;
			}
		}else{
			client = true;
			String clientClass = null;
			searching:
			try {
				ClassReader cr = new ClassReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(clientMainClass));
				ClassNode main = new ClassNode();
				cr.accept(main, ClassReader.EXPAND_FRAMES);
				MethodNode mainmethod = null;
				Iterator<MethodNode> iter = main.methods.iterator();
				while(iter.hasNext()){
					MethodNode next = iter.next();
					if(next.name.equals("main"))
						if(mainmethod == null)
							mainmethod = next;
						else
							throw new RuntimeException();
				}
				iter = null; //cleanup
				AbstractInsnNode insnnode = mainmethod.instructions.getLast();
				boolean foundMarker = false;
				while(insnnode != null){
					if(insnnode instanceof LdcInsnNode){
						LdcInsnNode ldc = (LdcInsnNode)insnnode;
						if(ldc.cst instanceof String){
							if(((String)ldc.cst).equals("Client thread")){
								foundMarker = true;
								insnnode = insnnode.getNext().getNext();
								break;
							}
						}
					}
					insnnode = insnnode.getPrevious();
				}
				/*
					LDC "Client thread">
			    	INVOKEVIRTUAL java/lang.Thread/setName(Ljava/lang/String)V
			    	aload 34
			    	invokevirtual net.minecraft.client.Minecraft.run() : void 
			    	return
			    */
				/*
					LDC "Client thread"
					INVOKEVIRTUAL java/lang/Thread.setName (Ljava/lang/String;)V
					NEW net/minecraft/client/Minecraft
					DUP
					ALOAD 42
					INVOKESPECIAL net/minecraft/client/Minecraft.<init> (Lnet/minecraft/client/main/GameConfiguration;)V
					INVOKEVIRTUAL net/minecraft/client/Minecraft.run ()V
				 */
				if(foundMarker){
					if(insnnode instanceof LabelNode){
						insnnode = insnnode.getNext();
						if(insnnode instanceof LineNumberNode)
							insnnode = insnnode.getNext();	
					}
					if(insnnode instanceof TypeInsnNode){
						if(insnnode.getOpcode() == Opcodes.NEW){
							TypeInsnNode test = (TypeInsnNode)insnnode;
							clientClass = test.desc+".class";
							break searching;
						}
					}
					if(insnnode instanceof VarInsnNode){
						int variableNumber = ((VarInsnNode)insnnode).var;
						System.out.println("variable number: "+variableNumber);
						foundMarker = false;
						while(insnnode != null){
							insnnode = insnnode.getPrevious();
							if(insnnode instanceof VarInsnNode){
								VarInsnNode test = (VarInsnNode)insnnode;
								if(test.getOpcode() == Opcodes.ASTORE && variableNumber == test.var){
									//found the assignment :D
									foundMarker = true;
									insnnode = insnnode.getPrevious();
									break;
								}
							}
						}
						if(foundMarker){
							/*
							724  invokespecial net.minecraft.client.Minecraft(net.minecraft.util.Session, int, int, boolean, boolean, java.io.File, java.io.File, java.io.File, java.net.Proxy, java.lang.String) [221]
    						727  astore 34
							*/
							if(insnnode instanceof MethodInsnNode){
								MethodInsnNode test = (MethodInsnNode) insnnode;
								if(test.getOpcode() == Opcodes.INVOKESPECIAL){
									//found it. finally. :} *wipes forehead*
									clientClass = test.owner+".class";
									
								}else{
									throw new RuntimeException();
								}
							}else{
								throw new RuntimeException();
							}
						}else{
							throw new RuntimeException();
						}
					}else{
						throw new RuntimeException();
					}
				}else{
					throw new RuntimeException();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			sidedClass = clientClass;
		}
		System.out.println("about to look for MC Version string in class '"+sidedClass+'\'');
		
		String foundVersionString = "";
		
		try {
			ClassReader cr = new ClassReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(sidedClass));
			MCVersionFinder finder = new MCVersionFinder();
			cr.accept(finder, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
			foundVersionString = finder.getVersionString(client);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.MCVersion = foundVersionString;
		if(this.MCVersion.matches(regex2)){
			this.snapshot = true;
			this.MCVersionRoot = this.MCVersion;
		}else{
			this.snapshot = false;
			final String[] mcVsplits = this.MCVersion.split("\\.");
			String mcvroot = "";
			{
				String add = "";
				String dot = ".";
				for(int cur=0;cur<ROOTSIZE && cur<mcVsplits.length;cur++){
					mcvroot += add;
					add = dot;
					mcvroot += mcVsplits[cur];
				}
			}
			this.MCVersionRoot = mcvroot;
		}
		
		System.out.println("PML: found Minecraft version - "+MCVersion+(this.snapshot ? " (snapshot)" : ""));
	}
	public final String MCVersion;
	public final String MCVersionRoot;
	public final boolean snapshot;
	
	static class MCVersionFinder extends ClassVisitor{
		public MCVersionFinder() {
			super(Opcodes.ASM4);
			this.methodvisitorinstance = new MCVersionFinder_MethodVisitor(this);
		}
		MCVersionFinder_MethodVisitor methodvisitorinstance;
		List<String> maybes = new ArrayList();
		String getVersionString(boolean client){
			String ret = null;
			for(String each : maybes){
				if(each.equals("##0.00"))//the decimal format from Minecraft.class
					continue;//ignore that one.
				if(client){//possibly "Minecraft #.#<.#>"
					String[] split = each.split("\\s");
					if(split.length == 2)
						each = split[1];
				}
				if(ret == null){
					ret = each;
				}else{
					if(!ret.equals(each))
						throw new RuntimeException("Couldn't figure out Minecraft version number: came across \""+ret+"\" and \""+each+'"');
				}
			}
			return ret;
		}
		void addMaybe(String found){
			this.maybes.add(found);
		}
		@Override
		public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
		}
		@Override
		public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
			return null;
		}
		@Override
		public void visitEnd() {
		}
		@Override
		public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		}
		@Override
		public void visitOuterClass(String arg0, String arg1, String arg2) {
		}
		@Override
		public void visitSource(String arg0, String arg1) {
		}
		
		@Override
		public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
			return methodvisitorinstance;
		}
		@Override
		public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
			return null;//currently the version string is only found in methods
		}
		class MCVersionFinder_MethodVisitor extends MethodVisitor{
			public MCVersionFinder_MethodVisitor(MCVersionFinder parent) {
				super(Opcodes.ASM4);
				this.parent = parent;
			}
			MCVersionFinder parent;
			public void visitLdcInsn(Object cst) {
		        if(cst instanceof String){
		        	String load = (String)cst;
		        	if(load.matches(regex1) || load.matches(regex2))
		        		this.parent.addMaybe(load);
		        }
		    }
		}
	}
}
