import com.yagasoft.overcast.google.Google;


public class _Test
{

	public static void main(String[] args)
	{
		Google google = new Google();
		google.buildFileTree(true);

//		FileMapping mapping = new FileMapping(google.getDriveService());
//		mapping.setLocal(new LocalFile(new File("G:\\Downloads\\test.png")));
//		mapping.upload("0ByO8YVIZubxxdTZUNUhHYTB5bFE");

//		FileMapping mapping = new FileMapping(google.getDriveService());
//
//		try
//		{
//			mapping.setRemote(new RemoteFile(google.getDriveService().files().get("0ByO8YVIZubxxVXpwLVRjbUJ0WTQ").execute()));
//			mapping.download("G:\\Downloads\\");
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}

//		FileMapping mapping = new FileMapping();
//
//		LocalFile local = new LocalFile()
//		{
//
//			@Override
//			public void updateInfo()
//			{}
//
//			@Override
//			public void rename(String newName)
//			{}
//
//			@Override
//			public void move(Folder folder)
//			{}
//
//			@Override
//			public void delete()
//			{}
//
//			@Override
//			public void copy(Folder folder)
//			{}
//
//			@Override
//			public long getSize()
//			{
//				return 0;
//			}
//		};
//		local.setPhysical(new File("G:\\Downloads\\Test.txt"));
//
//		RemoteFile remote = new RemoteFile(new com.google.api.services.drive.model.File());
//		remote.getApiFile().setId("0ByO8YVIZubxxM2FIT0tpY0lTTDQ");
//
//		mapping.setLocal(local);
//		mapping.setRemote(remote);
//
//		System.out.println(mapping.getRemote().getApiFile());
	}

}
